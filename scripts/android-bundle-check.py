#!/usr/bin/env python3
"""Check the actual bundle manifest and native ELF alignment, then record release provenance."""
import hashlib
import json
import os
from pathlib import Path
import struct
import subprocess
import sys
import xml.etree.ElementTree as ET
import zipfile


def check_bundle(aab, manifest, config):
    android = '{http://schemas.android.com/apk/res/android}'
    root = ET.parse(manifest).getroot()
    package = root.get('package')
    if package != 'dev.hsichen.colorinvo':
        raise ValueError('Bundle package must be dev.hsichen.colorinvo (never the debug package).')
    code = root.get(android + 'versionCode')
    name = root.get(android + 'versionName')
    if code != os.environ.get('ANDROID_VERSION_CODE', '1') or name != os.environ.get('ANDROID_VERSION_NAME', '0.1.0'):
        raise ValueError('Bundle version does not match ANDROID_VERSION_CODE / ANDROID_VERSION_NAME.')
    sdk = root.find('uses-sdk')
    if sdk is None or int(sdk.get(android + 'targetSdkVersion', '0')) < 36:
        raise ValueError('Play releases must target API 36 or later.')
    app = root.find('application')
    if app is None or app.get(android + 'debuggable') == 'true':
        raise ValueError('Release application is missing or debuggable.')
    permissions = [item.get(android + 'name') for item in root if item.tag.startswith('uses-permission')]
    # These normal permissions come from the WorkManager jobs used by Glance.
    allowed = {package + '.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION',
               'android.permission.WAKE_LOCK', 'android.permission.ACCESS_NETWORK_STATE',
               'android.permission.RECEIVE_BOOT_COMPLETED'}
    if set(permissions) - allowed:
        raise ValueError('New app permissions require a review of the privacy policy and Play declarations.')
    libraries = []
    with zipfile.ZipFile(aab) as bundle:
        for entry in bundle.namelist():
            if '/lib/' not in entry or not entry.endswith('.so'):
                continue
            data = bundle.read(entry)
            abi = entry.split('/lib/')[1].split('/')[0]
            if abi in ('armeabi-v7a', 'x86') and data[:6] == b'\x7fELF\x01\x01':
                counterpart = entry.replace('/armeabi-v7a/', '/arm64-v8a/').replace('/x86/', '/x86_64/')
                if counterpart not in bundle.namelist():
                    raise ValueError(f'{entry} is missing its required 64-bit counterpart.')
                libraries.append(entry)
                continue
            if abi not in ('arm64-v8a', 'x86_64') or data[:6] != b'\x7fELF\x02\x01':
                raise ValueError(f'Unexpected native ABI: {entry}; review 64-bit support and alignment.')
            offset = struct.unpack_from('<Q', data, 32)[0]
            size, count = struct.unpack_from('<HH', data, 54)
            for index in range(count):
                kind, _, _, _, _, _, _, alignment = struct.unpack_from('<IIQQQQQQ', data, offset + size * index)
                if kind == 1 and alignment < 16384:
                    raise ValueError(f'{entry} is not aligned for 16 KB memory pages.')
            libraries.append(entry)
    if libraries:
        packaging = json.loads(config.read_text()).get('optimizations', {}).get('uncompressNativeLibraries', {})
        if packaging.get('alignment') != 'PAGE_ALIGNMENT_16K':
            raise ValueError('Bundle must request 16 KB APK native-library alignment from Play.')
    return {'package': package, 'versionCode': int(code), 'versionName': name,
            'targetSdk': int(sdk.get(android + 'targetSdkVersion')), 'nativeLibraries': libraries,
            'sha256': hashlib.sha256(aab.read_bytes()).hexdigest()}


def main():
    try:
        aab, manifest, config = map(Path, sys.argv[1:])
        result = check_bundle(aab, manifest, config)
        result['commit'] = subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip()
        result['workingTreeDirty'] = bool(subprocess.check_output(['git', 'status', '--porcelain'], text=True).strip())
        (aab.parent / 'release-manifest.json').write_text(json.dumps(result, indent=2) + '\n')
        print(f"Bundle verified: {result['package']} {result['versionName']} ({result['versionCode']}), API {result['targetSdk']}, 16 KB native alignment.")
    except (ValueError, OSError, struct.error, ET.ParseError, zipfile.BadZipFile) as error:
        sys.exit(str(error))


if __name__ == '__main__':
    main()

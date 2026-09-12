import importlib.util
from pathlib import Path
import shutil
import struct
import tempfile
import unittest
import zlib

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('store', ROOT / 'scripts/android-store-check.py')
store = importlib.util.module_from_spec(spec)
spec.loader.exec_module(store)


class PlayListingTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.listing = Path(self.directory.name) / 'play'
        shutil.copytree(ROOT / 'apps/android/play', self.listing)

    def problems(self):
        return '\n'.join(store.check_listing(self.listing))

    def test_committed_listing_is_complete(self):
        self.assertEqual('', self.problems())

    def test_missing_feature_graphic_blocks_release(self):
        (self.listing / 'zh-TW/images/featureGraphic.png').unlink()
        self.assertIn('zh-TW/featureGraphic.png', self.problems())

    def test_tall_device_screenshots_block_release(self):
        path = self.listing / 'en-US/images/phoneScreenshots/1_wallpaper.png'
        data = bytearray(path.read_bytes())
        data[20:24] = struct.pack('>I', 2400)
        data[29:33] = struct.pack('>I', zlib.crc32(data[12:29]))
        path.write_bytes(data)
        self.assertIn('invalid Play screenshot dimensions 1080×2400', self.problems())

    def test_alpha_feature_graphic_blocks_release(self):
        shutil.copy(self.listing / 'en-US/images/icon.png', self.listing / 'en-US/images/featureGraphic.png')
        self.assertIn('8-bit RGB', self.problems())

    def test_duplicate_screenshots_do_not_satisfy_minimum(self):
        folder = self.listing / 'en-US/images/phoneScreenshots'
        shutil.copy(folder / '1_wallpaper.png', folder / '2_paint.png')
        self.assertIn('duplicate screenshot', self.problems())

    def test_overlong_localized_copy_blocks_release(self):
        (self.listing / 'zh-TW/short_description.txt').write_text('條' * 81)
        self.assertIn('1–80 characters, got 81', self.problems())

    def test_damaged_png_blocks_release(self):
        path = self.listing / 'en-US/images/icon.png'
        data = bytearray(path.read_bytes())
        data[20] ^= 1
        path.write_bytes(data)
        self.assertIn('corrupt PNG chunk', self.problems())


if __name__ == '__main__':
    unittest.main()

#!/usr/bin/env python3
"""Require publicly reachable Android policy text before a Play submission."""
import sys
import urllib.request

for path in ('/en/privacy', '/privacy'):
    url = 'https://colorinvo.hsichen.dev' + path
    try:
        request = urllib.request.Request(url, headers={'User-Agent': 'ColorInvo-Release-Check/1.0'})
        with urllib.request.urlopen(request, timeout=20) as response:
            text = response.read().decode('utf-8')
            if response.status != 200 or 'text/html' not in response.headers.get('Content-Type', ''):
                raise ValueError('Expected a public HTML policy')
            if not all(term in text for term in ('Android', 'Google', 'its.hsichen@gmail.com')):
                raise ValueError('The deployed policy does not yet include Android handling and support contact')
        print(f'Public Android privacy policy verified: {url}')
    except (OSError, ValueError) as error:
        sys.exit(f'{url}: {error}. Deploy the updated web policy before submitting to Play.')

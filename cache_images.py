import os
import sys
import re
import requests
import mimetypes

SCRIPT_DIR = 'iOS'
OUTPUT_DIR = 'assets/ios'

image_regex = re.compile(r'<Image[^>]*src="([^"]+)"[^>]*/>', re.IGNORECASE)

def get_extension_from_content_type(content_type):
    if not content_type:
        return '.jpg'  # fallback
    ext = mimetypes.guess_extension(content_type.split(';')[0].strip())
    if not ext:
        return '.jpg'
    if ext == '.jpe':
        return '.jpg'
    return ext

def download_image(url, save_path_base, headers={
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.117 Safari/537.36'
}):
    try:
        response = requests.get(url)
        response.raise_for_status()

        content_type = response.headers.get('Content-Type', '')
        ext = get_extension_from_content_type(content_type)

        save_path = save_path_base + ext

        with open(save_path, 'wb') as f:
            f.write(response.content)

        print('Download: ' + url + ': ' + save_path)
        return save_path 

    except Exception as e:
        print(str(e) + '\nTo: ' + save_path_base)
        answer = raw_input('Continue? N, jpg, png: ').strip().lower()
        if answer == 'n':
            sys.exit(5)
        return save_path_base + '.' + answer
       

def process_file(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    matches = image_regex.findall(content)
    if not matches:
        return

    new_content = content
    file_name = os.path.splitext(os.path.basename(file_path))[0] + '-'

    for idx, url in enumerate(matches, 1):
        if url.startswith('/assets/'):
            continue
        save_path = os.path.join(OUTPUT_DIR, file_name + str(idx))
        
        save_path = download_image(url, save_path)
        
        new_content = new_content.replace(url, '/' + save_path)

    with open(file_path, 'w') as f:
        f.write(new_content)
    print('File: ' + file_path)

def main():
    for root, _, files in os.walk(SCRIPT_DIR):
        for file in files:
            if file.endswith('.mdx'):
                full_path = os.path.join(root, file)
                process_file(full_path)

if __name__ == '__main__':
    main()

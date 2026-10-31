import os, subprocess
from PIL import Image

res_dir = r'D:\traework_space\豆包软件\TapCounter\app\src\main\res'
nodpi = os.path.join(res_dir, 'drawable-nodpi')

# 1) download new icon (2048x2048 black-gold heart)
icon_url = 'https://aka.doubaocdn.com/s/KBYZUdVzBC'
icon_tmp = r'D:\traework_space\豆包软件\TapCounter\scripts\_icon_src.png'
subprocess.run(['curl', '-L', '-o', icon_tmp, icon_url], check=True)
img = Image.open(icon_tmp).convert('RGBA')
print('icon', img.size)

# 2) legacy launcher PNGs per density
for d, s in {'mipmap-mdpi': 48, 'mipmap-hdpi': 72, 'mipmap-xhdpi': 96,
             'mipmap-xxhdpi': 144, 'mipmap-xxxhdpi': 192}.items():
    out_dir = os.path.join(res_dir, d)
    os.makedirs(out_dir, exist_ok=True)
    img.resize((s, s), Image.LANCZOS).save(os.path.join(out_dir, 'ic_launcher.png'))

# 3) adaptive foreground 432x432
img.resize((432, 432), Image.LANCZOS).save(os.path.join(res_dir, 'drawable', 'ic_launcher_fg.png'))

# 4) pony backgrounds: main = pinkie pie 4 poses, alt = all-characters poster
for p, n in [(r'C:\Users\28215\AppData\Local\Doubao\User Data\ClipboardTemp\87a1bbb8-ca04-4eb7-a214-438c125851a8.png', 'bgimg_pony_main'),
             (r'C:\Users\28215\AppData\Local\Doubao\User Data\ClipboardTemp\e97dd34b-944d-45f9-acbb-3161844909f0.png', 'bgimg_pony_alt')]:
    im = Image.open(p).convert('RGB')
    scale = max(900 / im.width, 1600 / im.height)
    nw, nh = round(im.width * scale), round(im.height * scale)
    im = im.resize((nw, nh), Image.LANCZOS)
    l, t = (nw - 900) // 2, (nh - 1600) // 2
    im.crop((l, t, l + 900, t + 1600)).save(os.path.join(nodpi, n + '.jpg'), 'JPEG', quality=82)
    print(n, os.path.getsize(os.path.join(nodpi, n + '.jpg')) // 1024, 'KB')
print('done')

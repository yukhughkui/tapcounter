import os, subprocess
from PIL import Image

res_dir = r'D:\traework_space\豆包软件\TapCounter\app\src\main\res'

icon_url = 'https://aka.doubaocdn.com/s/al65j5Njm7'
icon_tmp = r'D:\traework_space\豆包软件\TapCounter\scripts\_icon_final_src.png'
subprocess.run(['curl', '-L', '-o', icon_tmp, icon_url], check=True)
img = Image.open(icon_tmp).convert('RGBA')
print('icon', img.size)

for d, s in {'mipmap-mdpi': 48, 'mipmap-hdpi': 72, 'mipmap-xhdpi': 96,
             'mipmap-xxhdpi': 144, 'mipmap-xxxhdpi': 192}.items():
    out_dir = os.path.join(res_dir, d)
    os.makedirs(out_dir, exist_ok=True)
    img.resize((s, s), Image.LANCZOS).save(os.path.join(out_dir, 'ic_launcher.png'))

img.resize((432, 432), Image.LANCZOS).save(os.path.join(res_dir, 'drawable', 'ic_launcher_fg.png'))
print('fg saved')

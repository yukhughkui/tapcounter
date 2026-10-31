import os
from PIL import Image

SRC = {
    'bgimg_kitty_party': r'D:\QQ\Tencent Files\2821533718\nt_qq\nt_data\Pic\2026-10\Thumb\04e5a9403206ee3564e69a5871755828_720.jpg',
    'bgimg_kitty_cinnamon': r'D:\QQ\Tencent Files\2821533718\nt_qq\nt_data\Pic\2026-10\Thumb\dc6811ce142f8cd29b634d60f85b234c_720.jpg',
    'bgimg_genshin': r'D:\QQ\Tencent Files\2821533718\nt_qq\nt_data\Pic\2026-10\Thumb\20390432ab9fdfdbb5e9bf17223af45a_720.jpg',
    'bgimg_pony_xmas': r'D:\QQ\Tencent Files\2821533718\nt_qq\nt_data\Pic\2026-10\Ori\b2ee0b9171d20c5e7f13fdebc373c72e.jpg',
    'bgimg_pony_six': r'C:\Users\28215\AppData\Local\Doubao\User Data\ClipboardTemp\a1cf433a-4387-46ab-8e36-3198665f4be1.png',
    'bgimg_pony_room': r'C:\Users\28215\AppData\Local\Doubao\User Data\ClipboardTemp\20a6d8d2-80b0-44dc-8e76-70d90cf69392.png',
    'bgimg_pony_sky': r'C:\Users\28215\AppData\Local\Doubao\User Data\ClipboardTemp\59a98fe2-411d-4cd2-bca7-91214a0e5ed7.png',
    'bgimg_pony_starlight': r'C:\Users\28215\AppData\Local\Doubao\User Data\ClipboardTemp\d59596d4-31ac-4f8d-83ee-26c885d73a60.png',
}

OUT_DIR = r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\drawable-nodpi'
os.makedirs(OUT_DIR, exist_ok=True)

TARGET_W, TARGET_H = 900, 1600

for name, path in SRC.items():
    img = Image.open(path).convert('RGB')
    # center-crop 到 9:16
    tw, th = TARGET_W, TARGET_H
    iw, ih = img.size
    scale = max(tw / iw, th / ih)
    nw, nh = round(iw * scale), round(ih * scale)
    img = img.resize((nw, nh), Image.LANCZOS)
    left, top = (nw - tw) // 2, (nh - th) // 2
    img = img.crop((left, top, left + tw, top + th))
    out = os.path.join(OUT_DIR, name + '.jpg')
    img.save(out, 'JPEG', quality=82)
    print(name, '->', out, os.path.getsize(out) // 1024, 'KB')

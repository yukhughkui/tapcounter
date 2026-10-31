from PIL import Image
import os

src = r'D:\traework_space\豆包软件\手机之问介绍\assets\icon_heart_nobeam.png'
res = r'D:\traework_space\豆包软件\TapCounter\app\src\main\res'

im = Image.open(src).convert('RGBA')
w, h = im.size
side = min(w, h)
# 居中方形裁切（图标为正方形，直接取方形区域）
im = im.crop(((w - side)//2, (h - side)//2, (w + side)//2, (h + side)//2))

sizes = {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144, 'xxxhdpi': 192}
for d, s in sizes.items():
    out = os.path.join(res, f'mipmap-{d}', 'ic_launcher.png')
    im.resize((s, s), Image.LANCZOS).save(out)
    print('mipmap', d, s, os.path.getsize(out))

fg = os.path.join(res, 'drawable', 'ic_launcher_fg.png')
im.resize((1080, 1080), Image.LANCZOS).save(fg)
print('fg', 1080, os.path.getsize(fg))

# 背景改为与图标底色一致的纯深藏蓝
bg = '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z"
        android:fillColor="#0A1428" />
</vector>
'''
with open(os.path.join(res, 'drawable', 'ic_launcher_bg.xml'), 'w', encoding='utf-8') as f:
    f.write(bg)
print('bg written')

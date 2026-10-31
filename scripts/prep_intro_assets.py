# -*- coding: utf-8 -*-
import os, shutil, urllib.request

root = r'D:\traework_space\豆包软件\手机之问介绍'
assets = os.path.join(root, 'assets')
os.makedirs(assets, exist_ok=True)

# 1) download icon series (generated URLs)
icons = [
    ('icon_final.png', 'https://aka.doubaocdn.com/s/al65j5Njm7'),      # 定稿漫画问号
    ('icon_heart.png', 'https://aka.doubaocdn.com/s/KBYZUdVzBC'),     # 黑金爱心 v2.0
    ('icon_ask_a.png', 'https://aka.doubaocdn.com/s/P24JzzZt29'),     # 方向A 点痕之问
    ('icon_ask_b.png', 'https://aka.doubaocdn.com/s/bmtaGqQhZl'),     # 方向B 屏问
]
for name, url in icons:
    p = os.path.join(assets, name)
    if not os.path.exists(p):
        urllib.request.urlretrieve(url, p)
        print('downloaded', name, os.path.getsize(p) // 1024, 'KB')

# 2) copy four-theme background images
srcs = {
    'bgimg_kitty_party.jpg':    'bg_kitty_main.jpg',
    'bgimg_kitty_cinnamon.jpg': 'bg_kitty_alt.jpg',
    'bgimg_genshin.jpg':        'bg_genshin_main.jpg',
    'bgimg_genshin_alt.jpg':    'bg_genshin_alt.jpg',
    'bgimg_pony_main.jpg':      'bg_pony_main.jpg',
    'bgimg_pony_alt.jpg':       'bg_pony_alt.jpg',
}
nodpi = r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\drawable-nodpi'
for s, d in srcs.items():
    shutil.copy2(os.path.join(nodpi, s), os.path.join(assets, d))
    print('copied', d)
print('assets ready:', sorted(os.listdir(assets)))

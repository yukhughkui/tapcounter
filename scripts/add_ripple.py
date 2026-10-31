# -*- coding: utf-8 -*-
"""v2.0.2: 给所有 MaterialButton / MaterialCardView 加主题色点击涟漪"""
import re, io

files = [
    r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_main.xml',
    r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_history.xml',
    r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_app_rank.xml',
    r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_sessions.xml',
]

card_re = re.compile(
    r'(<com\.google\.android\.material\.card\.MaterialCardView\n'
    r'            android:layout_width="match_parent"\n'
    r'            android:layout_height="wrap_content"\n)'
    r'(            app:cardBackgroundColor)', re.M)
btn_re = re.compile(
    r'(<com\.google\.android\.material\.button\.MaterialButton\n)(            android:id=)', re.M)

for f in files:
    with io.open(f, 'r', encoding='utf-8') as fh:
        s = fh.read()
    s2 = card_re.sub(r'\1android:clickable="true"\n            app:rippleColor="?attr/themeAccent"\n\2', s)
    s2 = btn_re.sub(r'\1app:rippleColor="?attr/themeAccent"\n\2', s2)
    n_card = len(card_re.findall(s))
    n_btn = len(btn_re.findall(s))
    with io.open(f, 'w', encoding='utf-8', newline='') as fh:
        fh.write(s2)
    print(f.split('\\')[-1], 'cards+', n_card, 'btns+', n_btn)
print('done')

# -*- coding: utf-8 -*-
import io

files = [
    r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_main.xml',
    r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_history.xml',
    r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_app_rank.xml',
    r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_sessions.xml',
]
total = 0
for f in files:
    with io.open(f, 'r', encoding='utf-8') as fh:
        s = fh.read()
    n = s.count('app:rippleColor="?attr/themeAccent"')
    s = s.replace('app:rippleColor="?attr/themeAccent"', 'app:rippleColor="?attr/themePrimary"')
    with io.open(f, 'w', encoding='utf-8', newline='') as fh:
        fh.write(s)
    total += n
    print(f.split('\\')[-1], n)
print('total', total)

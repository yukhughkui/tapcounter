# -*- coding: utf-8 -*-
import re, io

f = r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout\activity_main.xml'
btn_re = re.compile(
    r'(<com\.google\.android\.material\.button\.MaterialButton\n)(\s+android:id=)', re.M)
with io.open(f, 'r', encoding='utf-8') as fh:
    s = fh.read()
n = len(btn_re.findall(s))
s2 = btn_re.sub(r'\1app:rippleColor="?attr/themeAccent"\n\2', s)
with io.open(f, 'w', encoding='utf-8', newline='') as fh:
    fh.write(s2)
print('activity_main buttons +', n)

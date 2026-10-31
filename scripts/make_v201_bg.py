import os
from PIL import Image

nodpi = r'D:\traework_space\豆包软件\TapCounter\app\src\main\res\drawable-nodpi'

jobs = [
    (r'D:\QQ\Tencent Files\2821533718\nt_qq\nt_data\Pic\2026-10\Thumb\566bca58e079336fcd6752eaeb79dbc6_720.jpg', 'bgimg_pony_main'),
    (r'D:\QQ\Tencent Files\2821533718\nt_qq\nt_data\Pic\2026-10\Ori\2f5efe5adfb8cdf07af3ea95c7e9731a.jpg', 'bgimg_pony_alt'),
    (r'D:\QQ\Tencent Files\2821533718\nt_qq\nt_data\Pic\2026-10\Thumb\50bbab3e46399d2d6ef00748abc5c7e9_720.jpg', 'bgimg_genshin'),
    (r'D:\QQ\Tencent Files\2821533718\nt_qq\nt_data\Pic\2026-10\Thumb\2d182d328c92438093428bda8e4a8a1e_720.jpg', 'bgimg_genshin_alt'),
]
for p, n in jobs:
    im = Image.open(p).convert('RGB')
    scale = max(900 / im.width, 1600 / im.height)
    nw, nh = round(im.width * scale), round(im.height * scale)
    im = im.resize((nw, nh), Image.LANCZOS)
    l, t = (nw - 900) // 2, (nh - 1600) // 2
    im.crop((l, t, l + 900, t + 1600)).save(os.path.join(nodpi, n + '.jpg'), 'JPEG', quality=82)
    print(n, os.path.getsize(os.path.join(nodpi, n + '.jpg')) // 1024, 'KB')
print('done')

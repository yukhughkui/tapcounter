# -*- coding: utf-8 -*-
"""生成高级 launcher 图标资产：
1) adaptive foreground 透明底 PNG（432px = 4x，符号位于中心安全区）
2) adaptive 渐变背景 PNG 用不到（用 vector drawable），此处只产 foreground
"""
import math
from PIL import Image, ImageDraw

SIZE = 432  # 4x @ 108dp
CX, CY = 216, 216

img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

def ring(cx, cy, r, width, color):
    bbox = (cx - r, cy - r, cx + r, cy + r)
    d.arc(bbox, start=-45, end=135, fill=color, width=width)

# 涟漪弧（右下扩散的两段弧：0°~180° 的下半圆方向改为左上发散）
# 弧从 -40° 画到 140°（上方大半圈，营造从触点向上扩散的波纹）
GOLD = (191, 161, 95, 255)
GOLD_LIGHT = (232, 217, 174, 255)

# 外弧（浅金）
ring(CX, CY, 132, 16, GOLD_LIGHT)
# 内弧（金）
ring(CX, CY, 88, 18, GOLD)
# 中心点击点（白，带细深色描边形成层次）
d.ellipse((CX - 50, CY - 50, CX + 50, CY + 50), fill=(255, 255, 255, 255),
          outline=(18, 38, 61, 255), width=6)

# 触点点（左上小点：指尖落点，浅金实心）
d.ellipse((CX - 92, CY - 66, CX - 64, CY - 38), fill=GOLD)

img.save(r"D:\traework_space\豆包软件\TapCounter\app\src\main\res\drawable\ic_launcher_fg.png")
print("saved foreground png, size", img.size)

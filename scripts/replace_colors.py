# -*- coding: utf-8 -*-
import re, sys, os

base = r"D:\traework_space\豆包软件\TapCounter\app\src\main\res\layout"
files = ["activity_main.xml", "activity_history.xml", "activity_sessions.xml", "activity_app_rank.xml"]

# 替换表：旧引用 -> 新主题属性（key 按长度降序处理，避免 primary 吃掉 primary_dark）
mapping = {
    "@color/primary_dark": "?attr/themePrimaryDark",
    "@color/primary_light": "?attr/themePrimaryLight",
    "@color/primary": "?attr/themePrimary",
    "@color/accent_soft": "?attr/themeAccentSoft",
    "@color/accent": "?attr/themeAccent",
    "@color/card_border": "?attr/themeCardBorder",
    "@color/card": "?attr/themeCard",
    "@color/text_primary": "?attr/themeTextPrimary",
    "@color/text_secondary": "?attr/themeTextSecondary",
    "@color/bg": "?attr/themeBg",
    "#9FB2C9": "?attr/themeCardOnDark",
    "#2A3F5C": "?attr/themeCardDivider",
}

def replace_all(content):
    for key in sorted(mapping.keys(), key=len, reverse=True):
        content = content.replace(key, mapping[key])
    return content

ok = True
for f in files:
    p = os.path.join(base, f)
    with open(p, "r", encoding="utf-8") as fh:
        content = fh.read()
    new = replace_all(content)
    if new != content:
        with open(p, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(new)
        print("updated", f)
    else:
        print("no change", f)
    # 检查是否还有残留 @color/ 引用（除 ic_launcher_background 之类无关的）
    remains = re.findall(r"@color/[A-Za-z_]+", new)
    if remains:
        print("  remains:", sorted(set(remains)))
        ok = False

print("done, all_ok=", ok)

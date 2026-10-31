import subprocess, re, os, sys
apk = sys.argv[1] if len(sys.argv) > 1 else r'D:\traework_space\豆包软件\点击计数_v1.9.apk'
aapt = r'D:\traework_space\豆包软件\android-sdk\build-tools\35.0.0\aapt2.exe'
out = subprocess.run([aapt, 'dump', 'badging', apk], capture_output=True).stdout.decode('utf-8', 'ignore')
m = re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", out)
print('PKG=%s CODE=%s VER=%s' % (m.group(1), m.group(2), m.group(3)))
print('size_mb=%.2f' % (os.path.getsize(apk) / 1048576))

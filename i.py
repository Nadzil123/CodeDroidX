#!/usr/bin/env python3
import os, re

MANIFEST = "app/src/main/AndroidManifest.xml"

def main():
    if not os.path.exists(MANIFEST):
        print("Manifest not found:", MANIFEST)
        return

    with open(MANIFEST, "r", encoding="utf-8") as f:
        s = f.read()

    # 1) pastikan android:exported ada dan true untuk activity yang punya intent-filter LAUNCHER
    # 2) paksa nama activity jadi .MainActivity

    # Ganti android:name apapun yang mengandung MainActivity jadi .MainActivity
    s2 = re.sub(
        r'(<activity\b[^>]*android:name=")[^"]*MainActivity(")',
        r'\1.MainActivity\2',
        s
    )

    # Kalau activity belum punya android:exported, tambahin android:exported="true"
    def add_exported(m):
        block = m.group(0)
        if 'android:exported=' in block:
            # paksa true
            block = re.sub(r'android:exported="[^"]*"', 'android:exported="true"', block)
            return block
        # sisipkan sebelum penutup >
        return block.replace("<activity", '<activity android:exported="true"', 1)

    # Target activity yang punya MAIN + LAUNCHER (biar aman)
    pattern = r'<activity\b[\s\S]*?<intent-filter>[\s\S]*?android\.intent\.action\.MAIN[\s\S]*?android\.intent\.category\.LAUNCHER[\s\S]*?</intent-filter>[\s\S]*?</activity>'
    s3 = re.sub(pattern, add_exported, s2, count=1)

    with open(MANIFEST, "w", encoding="utf-8") as f:
        f.write(s3)

    print("OK: Manifest fixed ->", MANIFEST)

if __name__ == "__main__":
    main()

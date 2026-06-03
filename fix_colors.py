import os
import re

LAYOUT_DIR = r"c:\Main Folder\Raihan Folder\Homework\S4\Pemrograman Aplikasi Bergerak\Project\Experimental\pab-frontend\app\src\main\res\layout"

def replace_in_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replace backgrounds
    content = content.replace('android:background="@color/gray_light"', 'android:background="?android:attr/colorBackground"')
    
    # Replace text colors
    content = content.replace('android:textColor="@color/black"', 'android:textColor="?attr/colorOnSurface"')
    content = content.replace('android:textColor="@color/gray"', 'android:textColor="?android:attr/textColorSecondary"')
    content = content.replace('android:textColor="@color/gray_dark"', 'android:textColor="?android:attr/textColorSecondary"')
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for root, dirs, files in os.walk(LAYOUT_DIR):
    for file in files:
        if file.endswith(".xml"):
            replace_in_file(os.path.join(root, file))

print("Done replacing hardcoded colors in XMLs.")

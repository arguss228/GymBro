#!/usr/bin/env python3
import os
import sys
import re
import shutil
from pathlib import Path

def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        original = content

        content = content.replace('package com.gymbro.', 'package com.obsession.')
        content = content.replace('import com.gymbro.', 'import com.obsession.')
        content = re.sub(r'\bcom\.gymbro\b', 'com.obsession', content)

        content = content.replace('GymBroApplication', 'ObsessionApplication')
        content = content.replace('GymBroDatabase', 'ObsessionDatabase')
        content = content.replace('GymBroNavHost', 'ObsessionNavHost')
        content = content.replace('GymBroBottomNavBar', 'ObsessionBottomNavBar')
        content = content.replace('GymBroTheme', 'ObsessionTheme')
        content = content.replace('GymBroTypography', 'ObsessionTypography')
        content = content.replace('Theme.GymBro', 'Theme.Obsession')
        content = content.replace('"gymbro.db"', '"obsession.db"')

        if content != original:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"Ошибка при обработке {filepath}: {e}", file=sys.stderr)
        return False

try:
    base_path = Path('C:\\Users\\Oleg\\AndroidStudioProjects\\GymBro\\app\\src')

    # Создаем новую структуру директорий
    old_package_path = base_path / 'main' / 'java' / 'com' / 'gymbro' / 'app'
    new_package_path = base_path / 'main' / 'java' / 'com' / 'obsession' / 'app'

    if old_package_path.exists() and not new_package_path.exists():
        print(f"Копирую пакет из {old_package_path}")
        shutil.copytree(old_package_path, new_package_path)
        print(f"Создана новая структура: {new_package_path}")

    # Обновляем все .kt и .xml файлы в новом пакете
    updated = 0
    for kt_file in new_package_path.glob('**/*.kt'):
        if replace_in_file(kt_file):
            updated += 1
            print(f"Обновлен: {kt_file}")

    xml_files = list(base_path.glob('**/res/**/*.xml'))
    for xml_file in xml_files:
        if replace_in_file(xml_file):
            updated += 1
            print(f"Обновлен: {xml_file}")

    pro_file = Path('C:\\Users\\Oleg\\AndroidStudioProjects\\GymBro\\app\\proguard-rules.pro')
    if pro_file.exists():
        if replace_in_file(pro_file):
            updated += 1
            print(f"Обновлен: {pro_file}")

    # Удаляем старый пакет
    if old_package_path.exists():
        print(f"\nУдаляю старый пакет {old_package_path}")
        shutil.rmtree(old_package_path)
        # Очищаем пустые директории
        for parent in [old_package_path.parent, old_package_path.parent.parent]:
            if parent.exists() and not list(parent.iterdir()):
                parent.rmdir()

    print(f"\nВсего обновлено файлов: {updated}")

except Exception as e:
    print(f"Ошибка: {e}", file=sys.stderr)
    sys.exit(1)


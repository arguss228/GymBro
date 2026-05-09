#!/usr/bin/env python3
import os
import sys
from pathlib import Path

# Функция для замены содержимого файла
def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        original = content

        # Замены пакета и импортов
        content = content.replace('package com.gymbro.', 'package com.obsession.')
        content = content.replace('import com.gymbro.', 'import com.obsession.')
        content = re.sub(r'\bcom\.gymbro\b', 'com.obsession', content)

        # Замены классов и функций
        content = content.replace('class GymBroApplication', 'class ObsessionApplication')
        content = content.replace('class GymBroDatabase', 'class ObsessionDatabase')
        content = content.replace('fun GymBroNavHost', 'fun ObsessionNavHost')
        content = content.replace('fun GymBroBottomNavBar', 'fun ObsessionBottomNavBar')

        # Замены использования
        content = content.replace('GymBroApplication', 'ObsessionApplication')
        content = content.replace('GymBroDatabase', 'ObsessionDatabase')
        content = content.replace('GymBroNavHost', 'ObsessionNavHost')
        content = content.replace('GymBroBottomNavBar', 'ObsessionBottomNavBar')
        content = content.replace('GymBroTheme', 'ObsessionTheme')
        content = content.replace('GymBroTypography', 'ObsessionTypography')
        content = content.replace('Theme.GymBro', 'Theme.Obsession')
        content = content.replace('"gymbro.db"', '"obsession.db"')

        # Если содержимое изменилось, сохраняем
        if content != original:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"Ошибка при обработке {filepath}: {e}", file=sys.stderr)
        return False

# Основная логика
try:
    import re

    base_path = Path('C:\\Users\\Oleg\\AndroidStudioProjects\\GymBro\\app\\src')

    # Обновляем все .kt файлы
    kt_files = list(base_path.glob('**/*.kt'))
    print(f"Найдено .kt файлов: {len(kt_files)}", file=sys.stderr)

    updated = 0
    for kt_file in kt_files:
        if replace_in_file(kt_file):
            updated += 1
            print(f"Обновлен: {kt_file}")

    # Обновляем .xml файлы
    xml_files = list(base_path.glob('**/*.xml'))
    print(f"Найдено .xml файлов: {len(xml_files)}", file=sys.stderr)

    for xml_file in xml_files:
        if replace_in_file(xml_file):
            updated += 1
            print(f"Обновлен: {xml_file}")

    # Обновляем proguard-rules.pro
    pro_file = Path('C:\\Users\\Oleg\\AndroidStudioProjects\\GymBro\\app\\proguard-rules.pro')
    if pro_file.exists():
        if replace_in_file(pro_file):
            updated += 1
            print(f"Обновлен: {pro_file}")

    print(f"\nВсего обновлено файлов: {updated}")

except Exception as e:
    print(f"Ошибка: {e}", file=sys.stderr)
    sys.exit(1)


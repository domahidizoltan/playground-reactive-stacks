import csv
from collections import namedtuple

from typing import List

CATEGORY = {
    'Smileys & Emotion': 'SMILEYS_AND_EMOTION',
    'People & Body': 'PEOPLE_AND_BODY',
    'Component': 'COMPONENT',
    'Animals & Nature': 'ANIMALS_AND_NATURE',
    'Food & Drink': 'FOOD_AND_DRINK',
    'Travel & Places': 'TRAVEL_AND_PLACES',
    'Activities': 'ACTIVITIES',
    'Objects': 'OBJECTS',
    'Symbols': 'SYMBOLS',
    'Flags': 'FLAGS'
}

Emoji = namedtuple('Emoji', 'code category name')


UNIQUE_CODES = set()


def parse_emojis(line, category):
    tokens = line.split()
    codes = [token for token in tokens[1:] if token.startswith('U+')]
    name = " ".join(tokens[len(codes)+1:])

    global UNIQUE_CODES
    emojis = list()
    for code in codes:
        # for now we will use emojis unique by code
        if code not in UNIQUE_CODES:
            UNIQUE_CODES.add(code)
            emojis.append(Emoji(code, category, name))
    return emojis


def collect_emojis_from(file_path) -> List[Emoji]:
    emojis = list()
    with open(file_path) as file:
        for line in file.readlines():
            line = line.strip()
            if line[:1].isnumeric():
                emojis += parse_emojis(line, category)
            else:
                category = CATEGORY.get(line)
    return emojis


def export_to_csv(emojis: List[Emoji], file_path):
    with open(file_path, 'w') as csv_file:
        keys = list(emojis[0]._asdict().keys())
        writer = csv.DictWriter(csv_file, delimiter=',', fieldnames=keys)
        writer.writeheader()
        for emoji in emojis:
            writer.writerow(emoji._asdict())


def main():
    emojis = collect_emojis_from('emojis.txt')
    if emojis:
        export_to_csv(emojis, 'emojis.csv')


main()

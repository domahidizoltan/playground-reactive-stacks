from locust import HttpLocust, TaskSet
import csv
from random import randint
import logging


EMOJI_CODES = list()
CODES_COUNT = 0


def load_codes():
    with open('emoji-data/emojis.csv') as csv_file:
        reader = csv.reader(csv_file)
        for row in reader:
            if row[0] == 'code':
                continue
            EMOJI_CODES.append(row[0])
    global CODES_COUNT
    CODES_COUNT = len(EMOJI_CODES)


def used_at_date(locust):
    idx = randint(0, CODES_COUNT-1)
    locust.client.post('/emojis/' + EMOJI_CODES[idx])


def list_recent(locust):
    locust.client.get('/emojis/recent?seconds=300')


class UserBehavior(TaskSet):
    load_codes()
    tasks = {used_at_date: 1,
             list_recent: 3}


class ApiUser(HttpLocust):
    task_set = UserBehavior
    min_wait = 50
    max_wait = 2000

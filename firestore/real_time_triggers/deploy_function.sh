#!/bin/bash
set -e

PUBSUB_TOPIC=projects/heroes-hat-dev/topics/surveyWriteTrigger

firebase functions:config:set \
real_time_triggers.pubsub_topic="${PUBSUB_TOPIC}"

firebase deploy --only functions:surveyWriteTrigger

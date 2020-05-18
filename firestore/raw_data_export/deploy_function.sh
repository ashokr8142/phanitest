#!/bin/bash
set -e

BUCKET_NAME=heroes-hat-firestore-raw
SCHEDULE_TIME="every day 02:00"

firebase functions:config:set \
firestore_raw.bucket_name="${BUCKET_NAME}" \
firestore_raw.schedule_time="${SCHEDULE_TIME}"

firebase deploy --only functions:scheduledFirestoreExport

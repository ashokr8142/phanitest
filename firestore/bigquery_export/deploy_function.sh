#!/bin/bash
set -e

# Storage bucket name for Firestore export.
BUCKET_NAME=heroes-hat-firestore-raw
# GCP project name for the project hosting BigQuery export.
BQ_PROJECT_ID=heroes-hat-dev
# BigQuery dataset id.
BQ_DATASET_ID=firestoreRawData

firebase functions:config:set \
firestore_raw.bucket_name="${BUCKET_NAME}" \
bigquery_export.bq_project_id="${BQ_PROJECT_ID}" \
bigquery_export.bq_dataset_id="${BQ_DATASET_ID}"

firebase deploy --only functions:bigqueryExportFromRawData

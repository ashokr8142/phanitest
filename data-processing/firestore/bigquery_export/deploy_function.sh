#!/bin/bash
set -e

# Storage bucket name for Firestore export.
BUCKET_NAME=heroes-hat-unc-dev-my-studies-firestore-raw-data
# GCP project name for the project hosting BigQuery export.
BQ_PROJECT_ID=heroes-hat-unc-dev-data
# BigQuery dataset id.
BQ_DATASET_ID=heroes_hat_unc_dev_my_studies_firestore_data

firebase functions:config:set \
firestore_raw.bucket_name="${BUCKET_NAME}" \
bigquery_export.bq_project_id="${BQ_PROJECT_ID}" \
bigquery_export.bq_dataset_id="${BQ_DATASET_ID}"

firebase deploy --only functions:bigqueryExportFromRawData

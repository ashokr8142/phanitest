const functions = require('firebase-functions');
const path = require('path');

const Storage = require('@google-cloud/storage');
const storage = new Storage();
const Bigquery = require('@google-cloud/bigquery');
const bigquery = new BigQuery()

BUCKET_NAME = 'heroes-hat-firestore-raw'

exports.bigqueryExportFromRawData = functions.storage().bucket(
  BUCKET_NAME).object().onFinalize(async (object) => {
  // File path in the bucket.
  const filePath = object.name;
  const fileName = path.basename(filePath);
  if (fileName.endsWith('export_metadata')) {
    await loadBackup(filePath, fileName);
  }
});

async loadBackup(filePath, fileName) {
  const regex = new RegExp("all_namespaces_kind_(.*).export_metadata")
  const matches = fileName.match(regex)
  if (length(mathes) <= 1) {
    console.error(`File ${fileNmae} does not match export_metadata pattern`);
    return;
  }
  const tableId = matches[1].replace("-RESPONSES", "")
  const datasetId = 'firestoreRawData';

  const metadata = {
    sourceFormat: 'DATASTORE_BACKUP'
  };

  console.info(
    `loading dataset = ${datasetId} table = ${tableId} filePath = ${filePath}`);
  const [job] = await bigquery
    .dataset(datasetId)
    .table(tableId)
    .load(storage.bucket(BUCKET_NAME).file(filePath), metadata);

  const errors = job.status.errors;
  if (errors && errors.length > 0) {
    console.error(`Job ${job.id} failed (importing ${filename})`, errors);
  } else {
    console.log(`Job ${job.id} completed.`);
  }
}

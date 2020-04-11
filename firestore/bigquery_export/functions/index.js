const functions = require('firebase-functions');
const path = require('path');

const {Storage} = require('@google-cloud/storage');
const storage = new Storage();
const {BigQuery} = require('@google-cloud/bigquery');
const bigquery = new BigQuery();

BUCKET_NAME = 'heroes-hat-firestore-raw';

exports.bigqueryExportFromRawData = functions.storage.bucket(
  BUCKET_NAME).object().onFinalize(async (object) => {
  // File path in the bucket.
  const filePath = object.name;
  const fileName = path.basename(filePath);
  if (fileName.endsWith('export_metadata')) {
    await loadBackup(filePath, fileName);
  }
});

async function loadBackup(filePath, fileName) {
  const regex = new RegExp("all_namespaces_kind_(.*).export_metadata");
  const matches = fileName.match(regex);
  if (!matches || matches.length <= 1) {
    console.info(`File ${fileName} does not match export_metadata pattern`);
    return;
  }
  const tableId = matches[1].replace("-RESPONSES", "").replace(/\W/g, "");
  const datasetId = 'firestoreRawData';

  const metadata = {
    sourceFormat: 'DATASTORE_BACKUP',
    writeDisposition: 'WRITE_TRUNCATE'
  };

  console.info(
    `loading dataset = ${datasetId} table = ${tableId} filePath = ${filePath}`);
  const [job] = await bigquery
    .dataset(datasetId)
    .table(tableId)
    .load(storage.bucket(BUCKET_NAME).file(filePath), metadata);

  const errors = job.status.errors;
  if (errors && errors.length > 0) {
    console.error(`Job ${job.id} failed (importing ${fileName})`, errors);
  } else {
    console.log(`Job ${job.id} completed.`);
  }
}

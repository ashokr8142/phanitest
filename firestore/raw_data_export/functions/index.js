const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();


exports.scheduledFirestoreExport = functions.pubsub.schedule(
  'every day 02:00').onRun(
    async context => {
  // Get all colletion ids under root.
  const collections = await admin.firestore().listCollections();
  const collectionIds = collections.map(c => c.path);

  const databaseName = client.databasePath(
    process.env.GCLOUD_PROJECT,
    '(default)'
  );
  const outputBucketPath = 'gs://heroes-hat-firestore-raw'
  const operationInfo = await admin.firestore().exportDocuments(
    {databaseName, collectionIds, outputUriPrefix: outputBucketPath});
  console.log(operationInfo);
});

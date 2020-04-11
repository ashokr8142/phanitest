// To trigger the job manually click "Run Now" in Cloud Scheduler console:
// https://screenshot.googleplex.com/8Oa8Cz6GY3h

const functions = require('firebase-functions');
const firestore = require('@google-cloud/firestore');
const admin = require('firebase-admin');

const client = new firestore.v1.FirestoreAdminClient();
admin.initializeApp();

exports.scheduledFirestoreExport = functions.pubsub.schedule(
  'every day 02:00').onRun(
    async context => {
  // Get all colletion ids under root.
  const collections = await admin.firestore().listCollections();
  var collectionIds = collections.map(c => c.path);

  /* eslint-disable no-await-in-loop */
  for (const collection of collections) {
    collectionIds.push.apply(collectionIds, await getSubCollecitons(
      collection));
  }
  /* eslint-enable no-await-in-loop */
  collectionIds =  Array.from(new Set(collectionIds));
  console.info(`Collection ids to export = ${collectionIds.join(',')}`);

  const projectId = process.env.GCP_PROJECT || process.env.GCLOUD_PROJECT;
  const databaseName = client.databasePath(projectId, '(default)');
  const outputBucketPath = 'gs://heroes-hat-firestore-raw'

  return client.exportDocuments({
    name: databaseName,
    collectionIds: collectionIds,
    outputUriPrefix: outputBucketPath}).then(responses => {
      const response = responses[0];
      console.log(`Operation Name: ${response['name']}`);
      return;
    })
    .catch(err => {
      console.error(err);
    });
});

async function getSubCollecitons(collection) {
  var collectionIds = [];
  /* eslint-disable no-await-in-loop */
  for (const doc of (await collection.listDocuments())) {
    collections = await doc.listCollections();
    collectionIds.push.apply(collectionIds, collections.map(c => {
      return c.id;
    }));
    for (const subcollection of collections) {
      collectionIds.push.apply(collectionIds, await getSubCollecitons(
        subcollection));
    }
  }
  /* eslint-enable no-await-in-loop */
  return collectionIds;
}

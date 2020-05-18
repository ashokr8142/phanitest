const admin = require('firebase-admin');
const functions = require('firebase-functions');
const {PubSub} = require('@google-cloud/pubsub');
const pubSubClient = new PubSub();

admin.initializeApp();

const globalTopicName = functions.config().real_time_triggers.pubsub_topic;

// Special value to ignore when calculating score (means participant did not
// want to answer).
const ignoreValue = -999;

exports.surveyWriteTrigger = functions.firestore
  .document('{studyResponses}/{studyId}/Activities/{activityUid}')
  .onWrite(async (change, context) => {
    // Contains the data that was written.
    const data = change.after.data();
    // Activity string id, e.g. PHQDep.
    const activityId = data.activityId;
    // Participant id identifies the user.
    const participantId = data.participantId;
    // Get handler for the study.
    try {
      await publishToGlobalTopic(activityId, participantId, data);
    } catch(error) {
      console.error(error);
      return -1;
    }
    return 0;
  });

async function publishToGlobalTopic(activityId, participantId, data) {
  // Wire the data with activityId.
  const message = {
    activityId: activityId,
    participantId: participantId,
    // Data can be different based on activity handler.
    data: data,
  };
  const dataBuffer = Buffer.from(JSON.stringify(message));

  try {
    const messageId = await pubSubClient.topic(
      globalTopicName).publish(dataBuffer);
    console.log(`Message ${messageId} published.`);
    logInfo(activityId, participantId, data);
  } catch(err) {
    console.error('Pulish message error :', err);
  }
}

const logInfo = (activityId, participantId, data) => {
  console.log(
    `Change in activity ${activityId}, participant ${participantId}`);
}

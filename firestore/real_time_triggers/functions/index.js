const admin = require('firebase-admin');
const functions = require('firebase-functions');
const {PubSub} = require('@google-cloud/pubsub');
const pubSubClient = new PubSub();

admin.initializeApp();

const globalTopicName = 'projects/heroes-hat-dev/topics/surveyWriteTrigger';

exports.surveyWriteTrigger = functions.firestore
  .document('{studyResponses}/{studyId}/Activities/{activityId}')
  .onWrite((change, context) => {
    const studyCollection = context.params.studyResponses;
    const studyId = context.params.studyId;
    const activityId = context.params.activityId;
    // Contains the data that was written.
    const data = change.after.data()
    // Get handler for the study.
    try {
      const handler = getStudyHandler(studyId);
      handler(studyCollection, studyId, activityId, data);
    } catch(error) {
      log.error(error);
    }
    return 0;
  });

const getStudyHandler = (studyId) => {
  // For now we push to common global topic, no matter the study.
  return publishToGlobalTopic;
}

const logInfo = (studyCollection, studyId, activityId, data) => {
  console.log(
    `Change in study ${studyId}, activity ${activityId},
    participant ${data.participantId}`);
}

const publishToGlobalTopic = (
  studyCollection,studyId, activityId, data) => {
  // For now wire whole response data, we may want to change it to collect
  // statistics first.
  const message = {
    studyCollection: studyCollection,
    studyId: studyId,
    activityId: activityId,
    response: data,
  };
  const dataBuffer = Buffer.from(JSON.stringify(message));

  pubSubClient.topic(globalTopicName).publish(dataBuffer).then(messageId => {
        console.log(`Message ${messageId} published.`);
        return 0;
      })
      .catch(err => {
        console.error('Pulish message error :', err);
        return 0;
      });
  logInfo(studyCollection, studyId, activityId, data);
}

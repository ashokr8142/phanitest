const admin = require('firebase-admin');
const functions = require('firebase-functions');
const {PubSub} = require('@google-cloud/pubsub');
const pubSubClient = new PubSub();

admin.initializeApp();

const globalTopicName = 'projects/heroes-hat-dev/topics/surveyWriteTrigger';

// Special value to ignore when calculating score (means participant did not
// want to answer).
const ignoreValue = -999;

// Specific activities ids.
const activityPHQDep = 'PHQDep';

exports.surveyWriteTrigger = functions.firestore
  .document('{studyResponses}/{studyId}/Activities/{activityUid}')
  .onWrite((change, context) => {
    // Contains the data that was written.
    const data = change.after.data()
    // Activity string id, e.g. PHQDep.
    const activityId = data.activityId;
    // Participant id identifies the user.
    const participantId = data.participantId;
    // Get handler for the study.
    try {
      const handler = getActivityHandler(activityId);
      handler(activityId, participantId, data);
    } catch(error) {
      log.error(error);
    }
    return 0;
  });

const getActivityHandler = (activityId) => {
  // All activities publish to common topic.
  if (activityId == activityPHQDep) {
    return publishResponseValuesSum;
  }
  // For now we push raw data for any other study.
  return publishToGlobalTopic;
}

const publishResponseValuesSum = (activityId, participantId, data) => {
  if (data.results.length == 0 || data.results[0].length == 0) {
    console.log(`Data empty for user ${participantId} activity ${activityId}.`)
    return;
  }
  const scoreSum = 0;
  const times = [];
  response = data.results[0];
  for (let [index, question_response] of response) {
    if (question_response.score != ignoreValue) {
      scoreSum = scoreSum + question_response.value;
      times.push(new Date(quesiton_response.endTime));
    }
  }
  publishToGlobalTopic(activityId, participantId, {
    individualResponseScoreSum: scoreSum,
    actvityEndTime: Math.max([new Date(0), Math.max(times)]).ToString()});
}

const publishToGlobalTopic = (activityId, participantId, data) => {
  // Wire the data with activityId.
  const message = {
    activityId: activityId,
    participantId: participantId,
    // Data can be different based on activity handler.
    data: data,
  };
  const dataBuffer = Buffer.from(JSON.stringify(message));

  pubSubClient.topic(globalTopicName).publish(dataBuffer).then(messageId => {
        console.log(`Message ${messageId} published.`);
        logInfo(activityId, participantId, data);
        return 0;
      })
      .catch(err => {
        console.error('Pulish message error :', err);
        return 0;
      });
}

const logInfo = (activityId, participantId, data) => {
  console.log(
    `Change in activity ${activityId}, participant ${participantId}`);
}

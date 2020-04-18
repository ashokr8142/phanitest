const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp();

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
  // For now we only log, no matter the study.
  return logInfo;
}

const logInfo = (studyCollection, studyId, activityId, data) => {
  console.log(
    `Change in study ${studyId}, activity ${activityId},
    participant ${data.participantId}`);
}

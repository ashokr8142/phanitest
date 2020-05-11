from bisect import bisect_right
from collections import namedtuple
import os
import time

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

_PROJECT_NAME = os.environ.get('PROJECT_ID')
_REPORT_SURVEYS = set(os.environ.get('USER_REPORT_SURVEYS').split(','))
_SURVEYS_COLLECTION_PREFIX = os.environ.get('SURVEYS_COLLECTION_PREFIX')

ActivityMsgs = namedtuple('ActivityMsgs', 'ranges msgs')

# TODO: This should be read from some kind of config file.
_ACTIVITY_ID_TO_MSG = {
  'PHQ-9': ActivityMsgs([0, 5, 10, 15, 20],
                        ['Minimal or No Symptoms',
                         'Mild Symptoms',
                         'Moderate Symptoms',
                         'Moderately Severe Symptoms',
                         'Severe Symptoms']),
  'GAD-7': ActivityMsgs([0, 5, 10, 15],
                        ['Minimal or No Symptoms',
                         'Mild Symptoms',
                         'Moderate Symptoms',
                         'Severe Symptoms']),
  'PSQI': ActivityMsgs([0, 1, 2, 3],
                       ['Very Good',
                        'Fairly Good',
                        'Fairly Bad',
                        'Very Bad']),
  'PCL-5': ActivityMsgs([0, 5],
                        ['Minimal or No Symptoms',
                         'Significant Symptoms']),
  'PHQDepWithSum': ActivityMsgs([0, 5, 10, 15, 20],
                        ['Minimal or No Symptoms',
                         'Mild Symptoms',
                         'Moderate Symptoms',
                         'Moderately Severe Symptoms',
                         'Severe Symptoms'])
}

def get_message_from_score(activity_id, score):
  if score is None:
    return 'Survey not completed'
  if activity_id not in _ACTIVITY_ID_TO_MSG:
    return 'Unknown survey'
  index = bisect_right(_ACTIVITY_ID_TO_MSG[activity_id].ranges, score)
  if index > 0:
    index -= 1
  return _ACTIVITY_ID_TO_MSG[activity_id].msgs[index]

def html_table_line(title, value):
  return '''<tr style="border:1px solid black;">
      <td>{}</td>
      <td>{}</td></tr>'''.format(title, value)

class UserReportHandler(object):

  def __init__(self):
    self.cred = credentials.ApplicationDefault()
    # For local debugging, use certificate.
    # self.cred = credentials.Certificate('/path/to/firestore/key.json')
    firebase_admin.initialize_app(self.cred, {
      'projectId': _PROJECT_NAME,
    })
    self.firebase_db = firestore.client()

  def create_report_html(self, participant_id):
    result = '<table style="border:1px solid black;border-collapse: collapse;">'
    for key, value in self.get_updated_score_map(participant_id).items():
      result += html_table_line(key, value)
    result += '</table>'
    return result

  def get_updated_score_map(self, participant_id):
    result = {activity_id:get_message_from_score(activity_id, None)
              for activity_id in _REPORT_SURVEYS}
    days_ago_8 = str((int(time.time()) - 60*60*24*8)*1000)
    survey_responses = self.firebase_db.collection(
      _SURVEYS_COLLECTION_PREFIX
      ).where(
        u'participantId', u'==', participant_id
      ).where(
       u'createdTimestamp', u'>=', days_ago_8
      ).order_by(
       u'createdTimestamp',
       direction=firestore.Query.ASCENDING
     ).stream()
    # Since the data is sorted in ascending order, when there are multiple
    # responses from the same activity, they will be overriden with the most
    # recent one.
    for response in survey_responses:
      response_data = response.to_dict()
      activity_id = response_data['activityId']
      if activity_id not in _REPORT_SURVEYS:
        continue
      score = 0
      for reply in response_data['results']:
        try:
          value = float(reply['value'])
        except ValueError:
          value = None
        if (value and value >= 0.0 and reply['key'] != '_SUM '):
          score += value
      result[activity_id] = get_message_from_score(activity_id, score)
    return result

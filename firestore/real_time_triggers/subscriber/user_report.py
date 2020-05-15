from bisect import bisect_right
from collections import namedtuple
import datetime
import logging
import os
import pytz

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

_PROJECT_NAME = os.environ.get('PROJECT_ID')
_REPORT_SURVEYS = set(os.environ.get('USER_REPORT_SURVEYS').split(','))
_SURVEYS_COLLECTION_PREFIX = os.environ.get('SURVEYS_COLLECTION_PREFIX')

ActivityMsgs = namedtuple('ActivityMsgs', 'title ranges text_colors')
TextAndColor = namedtuple('TextAndColor', 'text color')

# The app needs to be initialized only once, therefor intialize globally.
cred = credentials.ApplicationDefault()
# For local debugging, use certificate.
# cred = credentials.Certificate('/path/to/firestore/key.json')
firebase_admin.initialize_app(cred, { 'projectId': _PROJECT_NAME, })

_COLORS = {
  -1: '#FFFFFF', # white
  0: '#CEE0D4', # green
  1: '#FFF2CC', # yellow
  2: '#FBE5D6', # light red
  3: '#EAB0AB', # red
  4: '#EA8279' # dark red
}

# List of (activityId, ActivityMsgs). This is a list because we want fixed
# ordering of rows in the report HTML table.
# TODO: This should be read from some kind of config file.
_ACTIVITY_ID_TO_MSG_LIST = (
  ('PSQI2', ActivityMsgs('Your sleep quality was',
                        [0, 1, 2, 3],
                        [TextAndColor('Very Good', _COLORS[0]),
                         TextAndColor('Fairly Good', _COLORS[1]),
                         TextAndColor('Fairly Bad', _COLORS[2]),
                         TextAndColor('Very Bad', _COLORS[4])])),
  ('PHQ9+SH', ActivityMsgs('Your feelings of sadness or depression were',
                          [0, 5, 10, 15, 20],
                          [TextAndColor('None or minimal', _COLORS[0]),
                           TextAndColor('Mild', _COLORS[1]),
                           TextAndColor('Moderate', _COLORS[2]),
                           TextAndColor('Moderately Severe', _COLORS[3]),
                           TextAndColor('Severe', _COLORS[4])])),
  ('GAD7', ActivityMsgs('Your feelings of worry or anxiety were',
                       [0, 5, 10, 15],
                       [TextAndColor('None or minimal', _COLORS[0]),
                        TextAndColor('Mild', _COLORS[1]),
                        TextAndColor('Moderate', _COLORS[2]),
                        TextAndColor('Severe', _COLORS[4])])),
  ('PTSD2', ActivityMsgs('Symptoms of posttraumatic stress were',
                        [0, 6],
                        [TextAndColor('None or minimal', _COLORS[0]),
                         TextAndColor('Present', _COLORS[2])])),
  ('WSAS2', ActivityMsgs('''Your limitations in day-to-day life due to your
                        mental health were''',
                        [0, 10, 21],
                        [TextAndColor('None or mild', _COLORS[0]),
                         TextAndColor('Moderate', _COLORS[2]),
                         TextAndColor('Severe', _COLORS[4])])),
)

_ACTIVITY_ID_TO_MSG = {key: value for key, value in _ACTIVITY_ID_TO_MSG_LIST}

def get_message_from_score(activity_id, score):
  if score is None:
    return TextAndColor('Not completed', _COLORS[-1])
  if activity_id not in _ACTIVITY_ID_TO_MSG:
    return TextAndColor('Unknown survey', _COLORS[-1])
  index = bisect_right(_ACTIVITY_ID_TO_MSG[activity_id].ranges, score)
  if index > 0:
    index -= 1
  return _ACTIVITY_ID_TO_MSG[activity_id].text_colors[index]

def html_table_line(title, text_and_color):
  return '''
    <tr style="height:2.2rem;border:1px solid #999999;vertical-align:middle;">
      <td style="width:67%;border:1px solid #999999;">{}</td>
      <td style="width:33%;background-color:{};text-align:center;">{}</td>
    </tr>'''.format(
        title, text_and_color.color, text_and_color.text)

class UserReportHandler(object):

  def __init__(self):
    self.firebase_db = firestore.client()

  def create_report_html(self, participant_id, report_start_date):
    result = []
    result.append('<p><b>You reported that this past week:</b></p>')
    result.append(
        '<table style="border:1px solid #999999;border-collapse: collapse;">')
    updated_score_map = self.get_updated_score_map(participant_id,
                                                   report_start_date)
    for activity_id, activity_msgs in _ACTIVITY_ID_TO_MSG_LIST:
      value = updated_score_map.get(activity_id, None)
      if value:
        result.append(html_table_line(activity_msgs.title, value))
      else:
        logging.error('Failed to find entry for activity id: {}'.format(
            activity_id))
    result.append('</table>')
    result.append('''<p><b>About Your Report:</b> These Heroes Health reports
                  are to help you monitor your symptoms and mental health over
                  time. They should not be interpreted as a diagnosis. If you
                  have concerns about your mental health, contact your health
                  care provider and/or use the resources listed in the resources
                  tab.</p>''')
    return ''.join(result)

  def get_updated_score_map(self, participant_id, report_start_date):
    result = {activity_id:get_message_from_score(activity_id, None)
              for activity_id in _REPORT_SURVEYS}
    # We consider all surveys that are completed since 12PM Eastern time on
    # report_start_date.
    eastern_tz = pytz.timezone('US/Eastern')
    report_start_time = eastern_tz.localize(
        datetime.datetime(report_start_date.year, report_start_date.month,
                          report_start_date.day, hour=12))
    start_timestamp_milli = int(report_start_time.timestamp()) * 1000
    survey_responses = self.firebase_db.collection(
      _SURVEYS_COLLECTION_PREFIX
      ).where(
        u'participantId', u'==', participant_id
      ).where(
        u'createdTimestamp', u'>=', str(start_timestamp_milli)
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

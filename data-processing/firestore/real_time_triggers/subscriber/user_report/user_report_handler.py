import logging
import os

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

from user_report import response_data_util
from user_report import table_config

_PROJECT_NAME = os.environ.get('PROJECT_ID')
_SURVEYS_COLLECTION_PREFIX = os.environ.get('SURVEYS_COLLECTION_PREFIX')

# The app needs to be initialized only once, therefor intialize globally.
cred = credentials.ApplicationDefault()
# For local debugging, use certificate.
# cred = credentials.Certificate('/path/to/firestore/key.json')
firebase_admin.initialize_app(cred, { 'projectId': _PROJECT_NAME, })

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
    self.table_config = table_config.TableConfig.make_instance()

  def create_report_html(self, participant_id, report_start_date):
    result = []
    result.append('<p><b>You reported that this past week:</b></p>')
    result.append(
        '<table style="border:1px solid #999999;border-collapse: collapse;">')
    updated_score_map = self.get_updated_score_map(participant_id,
                                                   report_start_date)
    for activity_id, activity_msgs in self.table_config.get_activity_list():
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
    result = {
        activity_id: self.table_config.get_message_from_score(activity_id, None)
        for activity_id in self.table_config.get_activity_ids()}
    start_timestamp_millis = response_data_util.get_cutoff_timestamp_for_date(
        report_start_date)
    survey_responses = self.firebase_db.collection(
      _SURVEYS_COLLECTION_PREFIX
      ).where(
        u'participantId', u'==', participant_id
      ).where(
        u'createdTimestamp', u'>=', str(start_timestamp_millis)
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
      if activity_id not in self.table_config.get_activity_ids():
        continue
      score = response_data_util.compute_score_sum(response_data)
      result[activity_id] = self.table_config.get_message_from_score(
          activity_id, score)
    return result

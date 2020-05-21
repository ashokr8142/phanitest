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

class UserReportHandler(object):

  def __init__(self):
    self.firebase_db = firestore.client()
    self.table_config = table_config.TableConfig.make_instance()

  def create_report_html(self, participant_id, report_start_date):
    # Query Firestore for data.
    start_timestamp_millis = response_data_util.get_cutoff_timestamp_for_date(
        report_start_date)
    survey_responses = self._get_responses_from_firestore(
        participant_id, start_timestamp_millis)
    # Aggregate score sum for each survey.
    score_map = response_data_util.make_score_map(survey_responses)
    # Build HTML report based on score sum.
    result = []
    result.append('<p><b>You reported that this past week:</b></p>')
    result.append(self.table_config.make_html_table(score_map))
    result.append('''<p><b>About Your Report:</b> These Heroes Health reports
                  are to help you monitor your symptoms and mental health over
                  time. They should not be interpreted as a diagnosis. If you
                  have concerns about your mental health, contact your health
                  care provider and/or use the resources listed in the resources
                  tab.</p>''')
    return ''.join(result)

  # Gets all survey responses from Firestore for participant_id created after
  # start_timestamp_milli. Returns responses as a list of python dict sorted by
  # createdTimestamp in ascending order.
  def _get_responses_from_firestore(self, participant_id,
                                    start_timestamp_millis):
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
    return [response.to_dict() for response in survey_responses]

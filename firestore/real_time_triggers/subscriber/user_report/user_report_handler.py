from collections import namedtuple
import datetime
import logging
import os
import pytz

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

# Returns the start date for a report processed now.
def _get_report_start_date():
  # Survey cutoff is at noon on Sunday. Given that we don't know user's
  # timezone, we use Eastern timezone to be on the safer side. i.e. we expect
  # more users to fill in surveys right after they are scheduled than right
  # before they expire.
  eastern_tz = pytz.timezone('US/Eastern')
  eastern_now = datetime.datetime.now(eastern_tz)
  # Refer to the date 12 hours ago in Eastern timezone so that time before
  # noon counts as the previous day.
  reference_date = (eastern_now - datetime.timedelta(hours=12)).date()
  days_delta = reference_date.isoweekday() % 7
  return reference_date - datetime.timedelta(days=days_delta)

PersonalizedReport = namedtuple('PersonalizedReport',
                                'activity_date_time title content')

class UserReportHandler(object):

  def __init__(self):
    self.firebase_db = firestore.client()
    self.table_config = table_config.TableConfig.make_instance()

  # Creates reports for participant_id and returns it as a list of
  # PersonalizedReport.
  def create_reports(self, participant_id):
    current_week_start_date = _get_report_start_date()
    # Query Firestore for data.
    survey_responses = self._get_responses_from_firestore(participant_id)
    responses_by_week = response_data_util.group_by_week(
        survey_responses, current_week_start_date)
    now = datetime.datetime.now()
    result = []
    if len(responses_by_week) > 0:
      _, responses = responses_by_week.pop()
      current_week_content = self._create_current_week_content(responses)
      current_week_title = \
          'Summary Report: Week of {0:%B} {0:%d}, {0:%Y}'.format(
              current_week_start_date)
      result.append(PersonalizedReport(activity_date_time=now,
                                       title=current_week_title,
                                       content=current_week_content))
    # Also create Past Reports if there are responses from previous weeks.
    #
    # We want Past Reports to have an activity_date_time previous to current
    # week's activity_time because at serving time we render the list in
    # descending order by activity_date_time. Hence artificially use now - 1s as
    # the activity_date_time.
    if len(responses_by_week) > 0:
      past_content = self._create_past_report_content(responses_by_week)
      result.append(PersonalizedReport(
          activity_date_time=(now - datetime.timedelta(seconds=1)),
          title='Past Reports',
          content=past_content))
    return result

  # Returns HTML content for report of the current week.
  def _create_current_week_content(self, weekly_responses):
    # Aggregate score sum for each survey.
    score_map = response_data_util.make_score_map(weekly_responses)
    # Build HTML report based on score sum.
    result = []
    result.append('<style>')
    result.append(self.table_config.get_css())
    result.append('</style>')
    result.append('<p><b>You reported that this past week:</b></p>')
    result.append(self.table_config.make_html_table(score_map))
    result.append('''<p><b>About Your Report:</b> These Heroes Health reports
                  are to help you monitor your symptoms and mental health over
                  time. They should not be interpreted as a diagnosis. If you
                  have concerns about your mental health, contact your health
                  care provider and/or use the resources listed in the resources
                  tab.</p>''')
    return ''.join(result)

  # Returns HTML content for past reports.
  def _create_past_report_content(self, responses_by_week):
    result = []
    result.append('<style>')
    result.append(self.table_config.get_css())
    result.append('</style>')

    # Reverse the list so that past report runs on descending order.
    responses_by_week.reverse()
    for date, responses in responses_by_week:
      # Aggregate score sum for each survey in the week.
      score_map = response_data_util.make_score_map(responses)
      result.append('<p><b>Week of {0:%B} {0:%d}, {0:%Y}</b></p>'.format(date))
      result.append(self.table_config.make_html_table(score_map))

    result.append('''<p><b>About Your Report:</b> These Heroes Health reports
                  are to help you monitor your symptoms and mental health over
                  time. They should not be interpreted as a diagnosis. If you
                  have concerns about your mental health, contact your health
                  care provider and/or use the resources listed in the resources
                  tab.</p>''')
    return ''.join(result)

  # Gets all survey responses from Firestore for participant_id. Returns
  # responses as a list of python dict sorted by createdTimestamp in ascending
  # order.
  def _get_responses_from_firestore(self, participant_id):
    survey_responses = self.firebase_db.collection(
      _SURVEYS_COLLECTION_PREFIX
      ).where(
        u'participantId', u'==', participant_id
      ).order_by(
       u'createdTimestamp',
       direction=firestore.Query.ASCENDING
     ).stream()
    return [response.to_dict() for response in survey_responses]

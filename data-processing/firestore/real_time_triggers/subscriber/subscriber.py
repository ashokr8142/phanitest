import abc
import datetime
import json
import logging
import os
import pytz
import six
import smtplib
import sqlalchemy

from email.message import EmailMessage

from google.cloud.pubsub_v1 import SubscriberClient

import user_report

_DB_USER = os.environ.get("DB_USER")
_DB_PASS = os.environ.get("DB_PASS")
_DB_NAME = os.environ.get("DB_NAME")
_EMAIL_ADDRESS = os.environ.get("EMAIL_ADDRESS")
_EMAIL_PASS = os.environ.get("EMAIL_PASS")
_SQL_CONNECTION = os.environ.get("CLOUD_SQL_CONNECTION_NAME")
# Surveys that should trigger user report update.
_REPORT_SURVEYS = set(os.environ.get('USER_REPORT_SURVEYS').split(','))


class EmailSender(object):
  def __init__(self):
    self.server = smtplib.SMTP_SSL('smtp.gmail.com', 465)
    self.server.set_debuglevel(2)
    self.server.login(_EMAIL_ADDRESS, _EMAIL_PASS)

  def __delete__(self):
    self.server.close()

  def send(self, subject, body, recipients):
    """Send mail with given subject and body to resipients."""
    try:
      msg = EmailMessage()
      msg['Subject'] = subject
      msg['From'] = _EMAIL_ADDRESS
      msg['To'] = ', '.join(recipients)
      msg.set_content(body)
      self.server.send_message(msg, _EMAIL_ADDRESS, recipients)
    except Exception as e:
      logging.error('Had trouble sending email to: {}'.format(msg['To']))


@six.add_metaclass(abc.ABCMeta)
class Subscriber(object):
  def __init__(self, project_id, subscription_name, activity_id=None):
    self.subscriber_client = SubscriberClient()
    self.subscription_path = self.subscriber_client.subscription_path(
      project_id, subscription_name)
    self.activity_id = activity_id
    self.user_report = user_report.UserReportHandler()
    self.db = sqlalchemy.create_engine(
      sqlalchemy.engine.url.URL(
        drivername="mysql+pymysql",
        username=_DB_USER,
        password=_DB_PASS,
        database=_DB_NAME))
        # To run with local cloud proxy instance add query to the method above:
        # query={"unix_socket": "/cloudsql/{}".format(_SQL_CONNECTION)}

  def get_streaming_subscription(self):
    """Retruns streaming subscription on Google Pub/Sub topic."""
    subscription = self.subscriber_client.subscribe(
      self.subscription_path,
      callback=self.get_callback())
    return subscription

  def _get_user_details_id(self, participant_id):
    with self.db.connect() as conn:
      query = sqlalchemy.text(
        '''SELECT user_details_id FROM participant_study_info
         WHERE participant_id=:participant_id''')
      result = conn.execute(query, participant_id=participant_id).fetchone()
      if not result:
        raise ValueError(
          'Failed fetching user_id for participant {}'.format(
            participant_id))
      return result[0]

  @abc.abstractmethod
  def get_callback(self):
    """Defines callback invoked by the subscription."""

@six.add_metaclass(abc.ABCMeta)
class SubscriberWithUserReport(Subscriber):
  def __init__(self, project_id, subscription_name, activity_id=None):
     super(SubscriberWithUserReport, self).__init__(project_id,
                                                    subscription_name,
                                                    activity_id)

  def update_user_report(self, participant_id, study_id):
    try:
      user_id = self._get_user_details_id(participant_id)
      with self.db.connect() as conn:
        # The title contains date of the last Monday from the activity date.
        report_start_date = self._get_report_start_date()
        report_title = 'Summary Report: Week of {0:%B} {0:%d}, {0:%Y}'.format(
          report_start_date)
        report_content = self.user_report.create_report_html(participant_id,
                                                             report_start_date)
        study_info_id = self._get_study_id(study_id)
        insert_query = sqlalchemy.text(
          '''INSERT INTO personalized_user_report
              (activity_date_time, report_content, report_title, study_info_id,
              user_id)
            VALUES (:activity_date_time, :report_content, :report_title,
              :study_info_id, :user_id)
            ON DUPLICATE KEY UPDATE activity_date_time = :activity_date_time,
              report_content = :report_content, report_title = :report_title,
              study_info_id = :study_info_id, user_id = :user_id''')
        conn.execute(
          insert_query,
          activity_date_time=datetime.datetime.now(),
          report_content=report_content, report_title=report_title,
          study_info_id=study_info_id, user_id=user_id)
    except Exception as e:
      raise ValueError(
        '''Failed upsert into personalized_user_report table for
          participant {} study_id {}, exception {}'''.format(
            participant_id, study_id, e))

  # Returns the start date for a report processed now.
  def _get_report_start_date(self):
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

  def _get_study_id(self, study_name):
    with self.db.connect() as conn:
      query = sqlalchemy.text(
        '''SELECT id FROM study_info WHERE name=:study_name''')
      result = conn.execute(query, study_name=study_name).fetchone()
      if not result:
        raise ValueError(
          'Failed fetching study id for study name {}'.format(
            study_name))
      return result[0]

  @abc.abstractmethod
  def get_callback(self):
    """Defines callback invoked by the subscription."""


class TestSubscriber(Subscriber):
  """Subscriber to test the data format."""
  def __init__(self, project_id, subscription_name):
    super(TestSubscriber, self).__init__(project_id, subscription_name)

  def get_callback(self):

    def _callback(message):
      logging.info('Received message: {}'.format(message.data))
      message.ack()

    return _callback


class RoutingSubscriber(Subscriber):
  """Delegates work to subsubscribers or messages without explicit suscriber."""
  def __init__(self, project_id, subscription_name):
    # Map from activityId to subscriber
    self.subscriber_map = {
      'Demographics': DemographicsSubscriber(project_id, subscription_name)
    }
    for activity_id in _REPORT_SURVEYS:
      self.subscriber_map[activity_id] = HealthActivitySubscriber(
        project_id, subscription_name, activity_id)
    super(RoutingSubscriber, self).__init__(project_id, subscription_name)

  def get_callback(self):

    def _callback(message):
      data = json.loads(message.data)
      activity_id = data['activityId']
      if activity_id in self.subscriber_map:
        self.subscriber_map[activity_id].get_callback()(message)
      else:
        logging.info('Ignoring message from activity {}'.format(activity_id))
        message.ack()

    return _callback


class DemographicsSubscriber(Subscriber):
  """Subsctiber fro Demographics survey massages."""
  def __init__(self, project_id, subscription_name):
    super(DemographicsSubscriber, self).__init__(project_id, subscription_name,
                                                 'Demographics')

  def get_callback(self):

    def _callback(message):
      data = json.loads(message.data)
      if data['activityId'] == self.activity_id:
        institution_id = ''
        for result in data['data']['results']:
          if result['key'] == 'HealthcareSys':
            institution_id = result['value']
        participant_id = data['participantId']
        study_id = data['data']['studyId']
        logging.debug(
          '''Received Demogrphics message from participant {}
            with institution {}'''.format(participant_id, institution_id))
        try:
          self._insert_user_institution(participant_id, institution_id)
          message.ack()
        except Exception as e:
          logging.error(str(e))
          # Do not ack the message on expeception so it will be retried with
          # exponential backoff.
          message.nack()
      else:
        logging.info('Ignoring message from activity {}'.format(data['activityId']))
        message.nack()

    return _callback

  def _insert_user_institution(self, participant_id, institution_id):
    try:
      user_details_id = self._get_user_details_id(participant_id)
      with self.db.connect() as conn:
        insert_query = sqlalchemy.text(
          '''INSERT INTO user_institution (user_details_id, institution_id)
            VALUES (:user_details_id, :institution_id)
            ON DUPLICATE KEY UPDATE institution_id = :institution_id''')
        conn.execute(
          insert_query,
          user_details_id=user_details_id,
          institution_id=institution_id)
    except Exception as e:
      raise ValueError(
        '''Failed upsert into user_institution table for participant {}
         institution {}, exception {}'''.format(participant_id,
                                                institution_id, e))

class HealthActivitySubscriber(SubscriberWithUserReport):
  """Subscriber for activity survey messages."""
  def __init__(self, project_id, subscription_name, activity_id):
    # If you want to send an email on the survey completion uncomment this line.
    # Then use self._send_email in callback.
    # self.email_sender = EmailSender()
    super(HealthActivitySubscriber, self).__init__(
      project_id, subscription_name, activity_id)

  def get_callback(self):

    def _callback(message):
      data = json.loads(message.data)
      if data['activityId'] == self.activity_id:
        participant_id = data['participantId']
        study_id = data['data']['studyId']
        logging.info(
          'Received {} message from participant {}'.format(
            self.activity_id, participant_id))
        self.update_user_report(participant_id, study_id)
        message.ack()
      else:
        logging.info('Ignoring message from activity {}'.format(data['activityId']))
        message.nack()

    return _callback

  def _send_email(score_sum):
    email = self._get_participant_email(participant_id)
    if email:
      email_text = 'Your last {} survey score: {}.'.format(
        self.activity_id, score_sum)
      self.email_sender.send('{} survey result.'.format(self.activity_id),
                             email_text, [email])

  def _get_participant_email(self, participant_id):
    with self.db.connect() as conn:
      query = sqlalchemy.text(
        """SELECT user_details.email
         FROM participant_study_info
         JOIN user_details
         ON user_details.user_details_id=participant_study_info.user_details_id
         WHERE participant_study_info.participant_id=:participant_id""")
      result = conn.execute(query, participant_id=participant_id).fetchone()
      if result:
        return result[0]
      return None

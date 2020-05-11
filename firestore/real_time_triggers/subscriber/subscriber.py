import abc
import json
import logging
import os
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
      print('Had trouble sending email to: {}'.format(msg['To']))


@six.add_metaclass(abc.ABCMeta)
class Subscriber(object):
  def __init__(self, project_id, subscription_name, activity_id=None):
    self.subscriber_client = SubscriberClient()
    self.subscription_path = self.subscriber_client.subscription_path(
      project_id, subscription_name)
    self.activity_id = activity_id
    self.user_report = user_report.UserReportHandler()

  def get_streaming_subscription(self):
    """Retruns streaming subscription on Google Pub/Sub topic."""
    subscription = self.subscriber_client.subscribe(
      self.subscription_path,
      callback=self.get_callback())
    return subscription

  def _update_user_report(self, participant_id):
    # For now just print the content.
    print(self.user_report.create_report_html(participant_id))

  @abc.abstractmethod
  def get_callback(self):
    """Defines callback invoked by the subscription."""


class TestSubscriber(Subscriber):
  """Subscriber to test the data format."""
  def __init__(self, project_id, subscription_name):
    super(TestSubscriber, self).__init__(project_id, subscription_name)

  def get_callback(self):

    def _callback(message):
      print('Received message: {}'.format(message.data))
      message.ack()

    return _callback


class RoutingSubscriber(Subscriber):
  """Delegates work to subsubscribers or messages without explicit suscriber."""
  def __init__(self, project_id, subscription_name):
     # Map from activityId to subscriber.
     self.subscriber_map = {
      'PHQDepWithSum': PHQDepSubscriber(project_id, subscription_name),
      'Demographics': DemographicsSubscriber(project_id, subscription_name)
     }
     super(RoutingSubscriber, self).__init__(project_id, subscription_name)

  def get_callback(self):

    def _callback(message):
      data = json.loads(message.data)
      activity_id = data['activityId']
      if activity_id in self.subscriber_map:
        self.subscriber_map[activity_id].get_callback()(message)
      else:
        print('Ignoring message from activity {}'.format(activity_id))
        message.ack()

    return _callback


class DemographicsSubscriber(Subscriber):
  """Subsctiber fro Demographics survey massages."""
  def __init__(self, project_id, subscription_name):
    self.db = sqlalchemy.create_engine(
      sqlalchemy.engine.url.URL(
        drivername="mysql+pymysql",
        username=_DB_USER,
        password=_DB_PASS,
        database=_DB_NAME))
        # To run with local cloud proxy instance add query to the method above:
        # query={"unix_socket": "/cloudsql/{}".format(_SQL_CONNECTION)}
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
        print(
          '''Received Demogrphics message from participant {}
            with institution {}'''.format(participant_id, institution_id))
        try:
          self._insert_user_institution(participant_id, institution_id)
          self._update_user_report(participant_id)
          message.ack()
        except Exception as e:
          print(str(e))
          # Do not ack the message on expeception so it will be retried with
          # exponential backoff.
          message.nack()
      else:
        print('Ignoring message from activity {}'.format(data['activityId']))
        message.nack()

    return _callback

  def _insert_user_institution(self, participant_id, institution_id):
    with self.db.connect() as conn:
      query = sqlalchemy.text(
        '''SELECT user_details_id
         FROM participant_study_info
         WHERE participant_id=:participant_id''')
      result = conn.execute(query, participant_id=participant_id).fetchone()
      if not result:
        raise ValueError(
          'Failed fetching user_details_id for participant {}'.format(
            participant_id))
      user_details_id = result[0]
      insert_query = sqlalchemy.text(
        '''INSERT INTO user_institution (user_details_id, institution_id)
          VALUES (:user_details_id, :institution_id)
          ON DUPLICATE KEY UPDATE institution_id = :institution_id''')
      try:
        conn.execute(
          insert_query,
          user_details_id=user_details_id,
          institution_id=institution_id)
      except Exception as e:
        raise ValueError(
          '''Failed upsert into user_institution table for participant {}
           institution {}, exception {}'''.format(participant_id,
                                                  institution_id, e))

class PHQDepSubscriber(Subscriber):
  """Subscriber for PHQDep survey messages."""
  def __init__(self, project_id, subscription_name):
    self.db = sqlalchemy.create_engine(
      sqlalchemy.engine.url.URL(
        drivername="mysql+pymysql",
        username=_DB_USER,
        password=_DB_PASS,
        database=_DB_NAME))
        # To run with local cloud proxy instance add query to the method above:
        # query={"unix_socket": "/cloudsql/{}".format(_SQL_CONNECTION)}
    self.email_sender = EmailSender()
    super(PHQDepSubscriber, self).__init__(project_id, subscription_name,
                                           'PHQDepWithSum')

  def get_callback(self):

    def _callback(message):
      data = json.loads(message.data)
      if data['activityId'] == self.activity_id:
        score_sum = 0.0
        for result in data['data']['results']:
          if result['key'] == '_SUM':
            score_sum = score_sum + float(result['value'])
        participant_id = data['participantId']
        email = self._get_participant_email(participant_id)
        email_text = 'Your last PHQ survey score: {}.'.format(score_sum)
        print(
          'Received PHQDep message from participant {} with score {}'.format(
            participant_id, score_sum))
        if email:
          self.email_sender.send('PHQ survey result.', email_text, [email])
        self._update_user_report(participant_id)
        message.ack()
      else:
        print('Ignoring message from activity {}'.format(data['activityId']))
        message.nack()

    return _callback

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

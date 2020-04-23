import abc
import json
import logging
import os
import six
import smtplib
import sqlalchemy

from email.message import EmailMessage

from google.cloud.pubsub_v1 import SubscriberClient

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
    msg = EmailMessage()
    msg['Subject'] = subject
    msg['From'] = _EMAIL_ADDRESS
    msg['To'] = ', '.join(recipients)
    msg.set_content(body)
    self.server.send_message(msg, _EMAIL_ADDRESS, recipients)


@six.add_metaclass(abc.ABCMeta)
class Subscriber(object):
  def __init__(self, project_id, subscription_name):
    self.subscriber_client = SubscriberClient()
    self.subscription_path = self.subscriber_client.subscription_path(
      project_id, subscription_name)

  def get_streaming_subscription(self):
    """Retruns streaming subscription on Google Pub/Sub topic."""
    subscription = self.subscriber_client.subscribe(
      self.subscription_path,
      callback=self.get_callback())
    return subscription

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


class PHQDepSubscriber(Subscriber):
  """Subscriber for PHQDep survey messages."""
  def __init__(self, project_id, subscription_name):
    self.activity_id = 'PHQDepWithSum';
    self.db = sqlalchemy.create_engine(
      sqlalchemy.engine.url.URL(
        drivername="mysql+pymysql",
        username=_DB_USER,
        password=_DB_PASS,
        database=_DB_NAME))
        # To run with local cloud proxy instance add query to the method above:
        # query={"unix_socket": "/cloudsql/{}".format(_SQL_CONNECTION)}
    self.email_sender = EmailSender()
    super(PHQDepSubscriber, self).__init__(project_id, subscription_name)

  def get_callback(self):

    def _callback(message):
      data = json.loads(message.data)
      if data['activityId'] == self.activity_id:
        score_sum = 0.0
        for result in data['data']['results']:
          if result['key'] == '_SUM':
            score_sum = score_sum + float(result['value'])
        email = self._get_participant_email(data['participantId'])
        email_text = 'Your last PHQ survey score: {}.'.format(score_sum)
        print(
          'Received PHQDep message from participant {} with score {}'.format(
            data['participantId'], score_sum))
        if email:
          print('Email: ' + email)
          self.email_sender.send('PHQ survey result.', email_text, [email])
        message.ack()
      else:
        print('Ignoring message from activity {}'.format(data['activityId']))
        message.ack()

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

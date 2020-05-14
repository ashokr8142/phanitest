import logging
import os
import subscriber

_PROJECT_NAME = os.environ.get('PROJECT_ID')

def main():
  logging.basicConfig(level=logging.DEBUG,
                      format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s')
  logging.info('Initializing RoutingSubscriber.')
  sub_client = subscriber.RoutingSubscriber(_PROJECT_NAME, 'surveyWriteGlobal')
  logging.info('RoutingSubscriber initialized.')
  streaming_pull_future = sub_client.get_streaming_subscription()
  with sub_client.subscriber_client:
    try:
      streaming_pull_future.result()
    except Exception as e:
      print(e)
      streaming_pull_future.cancel()

if __name__ == '__main__':
  main()

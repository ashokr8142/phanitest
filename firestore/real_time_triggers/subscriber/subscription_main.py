import os
import subscriber

_PROJECT_NAME = os.environ.get('PROJECT_ID')

def main():
  sub_client = subscriber.RoutingSubscriber(_PROJECT_NAME, 'surveyPHQDep')
  streaming_pull_future = sub_client.get_streaming_subscription()
  with sub_client.subscriber_client:
    try:
      streaming_pull_future.result()
    except Exception as e:
      print(e)
      streaming_pull_future.cancel()

if __name__ == '__main__':
  main()

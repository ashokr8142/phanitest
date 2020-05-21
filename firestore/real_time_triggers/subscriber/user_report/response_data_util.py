"""Util functions for processing Firestore response data."""
import datetime
import pytz

# Computes score sum of response_data and returns it as a float.
def compute_score_sum(response_data):
  score = 0
  for reply in response_data['results']:
    try:
      value = float(reply['value'])
    # Ignore value that does not parse as number.
    except ValueError:
      value = None
    if (value and value >= 0.0 and reply['key'] != '_SUM'):
      score += value
  return score

# Computes score sum of each survey response and returns it as a map of
# activityId->scoreSum.
# If multiple responses use the same activityId, this function takes the last
# response in order of the list and ignores the previous ones.
def make_score_map(survey_responses):
  score_map = {}
  for response in survey_responses:
    activity_id = response['activityId']
    score = compute_score_sum(response)
    score_map[activity_id] = score
  return score_map

# Returns a millisecond UTC timestamp of the cutoff time for the given date.
# Cutoff time is 12PM noon in US Eastern timezone.
def get_cutoff_timestamp_for_date(date):
  eastern_tz = pytz.timezone('US/Eastern')
  time = eastern_tz.localize(
      datetime.datetime(date.year, date.month, date.day, hour=12))
  return int(time.timestamp()) * 1000

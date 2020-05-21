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

# Returns a millisecond UTC timestamp of the cutoff time for the given date.
# Cutoff time is 12PM noon in US Eastern timezone.
def get_cutoff_timestamp_for_date(date):
  eastern_tz = pytz.timezone('US/Eastern')
  time = eastern_tz.localize(
      datetime.datetime(date.year, date.month, date.day, hour=12))
  return int(time.timestamp()) * 1000

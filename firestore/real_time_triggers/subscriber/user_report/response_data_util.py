"""Util functions for processing Firestore response data."""
from collections import namedtuple
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

# Structure holding the Sunday of a week and all responses in that week.
WeeklyResponses = namedtuple('WeeklyResponses', 'date responses')

# Groups survey responses by week and returns a list of WeeklyResponses.
# Consumes the input list.
# The returned list is sorted by date in ascending order. Responses in each item
# is sorted by createdTimestamp in ascending order.
# If there is no response in a week, a WeeklyResponse with an empty list will
# still be added to the returned value.
# Requires:
#   survey_responses to be sorted by createdTimestamp in ascending order.
def group_by_week(survey_responses, current_week_start_date):
  result = []
  week_start_date = current_week_start_date
  week_start_timestamp = get_cutoff_timestamp_for_date(week_start_date)
  week_responses = []
  while len(survey_responses) > 0:
    response_timestamp = int(survey_responses[-1]['createdTimestamp'])
    # This response belongs to the current week.
    if response_timestamp >= week_start_timestamp:
      week_responses.append(survey_responses.pop())
      continue
    # This response belongs to a previous week. Add the current responses to
    # result and decrement week_start_date by 7 days.
    else:
      # Reverse the list so that it is in ascending order by createdTimestamp.
      week_responses.reverse()
      result.append(WeeklyResponses(week_start_date, week_responses))
      week_start_date -= datetime.timedelta(days=7)
      week_start_timestamp = get_cutoff_timestamp_for_date(week_start_date)
      week_responses = []
  # Add the last responses to result.
  result.append(WeeklyResponses(week_start_date, week_responses))
  # Reverse the list so that it is in ascending order by date.
  result.reverse()
  return result

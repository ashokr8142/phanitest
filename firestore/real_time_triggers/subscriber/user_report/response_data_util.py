"""Util functions for processing Firestore response data."""

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


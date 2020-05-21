"""Wrapper class for config file of HTML table report."""

from bisect import bisect_right
from collections import namedtuple
import json

ActivityMsgs = namedtuple('ActivityMsgs', 'title ranges text_colors')
TextAndColor = namedtuple('TextAndColor', 'text color')


def html_table_line(title, text_and_color):
  return '''
    <tr style="height:2.2rem;border:1px solid #999999;vertical-align:middle;">
      <td style="width:67%;border:1px solid #999999;">{}</td>
      <td style="width:33%;background-color:{};text-align:center;">{}</td>
    </tr>'''.format(
        title, text_and_color.color, text_and_color.text)

class TableConfig:
  def __init__(self, json_config):
    self._colors = json_config["colors"]
    self._default_color = self._colors[json_config["default-color"]]
    self._list = []
    self._dict = {}
    for questionnaire in json_config["questionnaires"]:
      id = questionnaire["id"]
      title = questionnaire["title"]
      ranges = []
      text_colors = []
      for bucket in questionnaire["buckets"]:
        lower_bound = bucket["lower_bound"]
        assert (len(ranges) == 0 or ranges[-1] < lower_bound), \
            "Items in buckets must be sorted by lower_bound in ascending order!"
        ranges.append(lower_bound)
        text_colors.append(
            TextAndColor(bucket["text"], self._colors[bucket["color-ref"]]))
      self._list.append((id, ActivityMsgs(title, ranges, text_colors)))
      self._dict = {key: value for key, value in self._list}

  # Factory method to create an instance from the config file.
  @staticmethod
  def make_instance():
    with open('app/user_report/table_config.json') as json_file:
      return TableConfig(json.load(json_file))

  # Returns a list of activity ids in order of the table report.
  def get_activity_ids(self):
    return [id for id, _ in self._list]

  # Returns an HTML table report for the given score_map.
  def make_html_table(self, score_map):
    htmls = []
    htmls.append(
        '<table style="border:1px solid #999999;border-collapse: collapse;">')
    for activity_id, activity_msgs in self._list:
      score = score_map.get(activity_id, None)
      text_and_color = self._get_message_from_score(activity_id, score)
      htmls.append(html_table_line(activity_msgs.title, text_and_color))
    htmls.append('</table>')
    return ''.join(htmls)

  # Returns a TextAndColor object corresponding to (activity_id, score)
  def _get_message_from_score(self, activity_id, score):
    if score is None:
      return TextAndColor('Not completed', self._default_color)
    if activity_id not in self._dict:
      return TextAndColor('Unknown survey', self._default_color)
    index = bisect_right(self._dict[activity_id].ranges, score)
    if index > 0:
      index -= 1
    return self._dict[activity_id].text_colors[index]

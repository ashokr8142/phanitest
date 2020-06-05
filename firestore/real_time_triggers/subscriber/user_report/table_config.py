"""Wrapper class for config file of HTML table report."""

from bisect import bisect_right
from collections import namedtuple
import json
import logging

ActivityMsgs = namedtuple('ActivityMsgs', 'title ranges text_colors')
TextAndColor = namedtuple('TextAndColor', 'text color')

_COMMON_CSS = '''
table, tr, td {
  border: 1px solid #999999;
}
table {
  border-collapse: collapse;
}
tr {
  height: 2.2rem;
  vertical-align: middle;
}
td.title {
  width: 67%;
}
td.value {
  width: 33%;
  text-align: center;
}
'''

def html_table_line(title, text_and_color):
  """Returns a row in HTML table."""
  return '''
    <tr>
      <td class="title">{}</td>
      <td class="value {}">{}</td>
    </tr>'''.format(
        title, text_and_color.color, text_and_color.text)

class TableConfig:
  def __init__(self, json_config):
    self._colors = json_config["colors"]
    assert (json_config["default-color"] in self._colors), \
        "Default color: {} is not defined in colors.".format(
            json_config["default-color"])
    self._default_color = json_config["default-color"]
    self._list = []
    self._dict = {}
    self._activity_id_to_canonical = {}
    for questionnaire in json_config["questionnaires"]:
      canonical_id = questionnaire["canonical_id"]
      title = questionnaire["title"]
      for activity_id in questionnaire["activity_ids"]:
        assert (activity_id not in self._activity_id_to_canonical), \
            "Found duplicated activity_id: '{}'.".format(activity_id)
        self._activity_id_to_canonical[activity_id] = canonical_id
      ranges = []
      text_colors = []
      for bucket in questionnaire["buckets"]:
        lower_bound = bucket["lower_bound"]
        assert (len(ranges) == 0 or ranges[-1] < lower_bound), \
            "Items in buckets must be sorted by lower_bound in ascending order!"
        assert (bucket["color-ref"] in self._colors), \
            "Color: {} is not defined".format(bucket["color-ref"])
        ranges.append(lower_bound)
        text_colors.append(
            TextAndColor(bucket["text"], bucket["color-ref"]))
      self._list.append((canonical_id, ActivityMsgs(title, ranges, text_colors)))
      self._dict = {key: value for key, value in self._list}

  @staticmethod
  def make_instance():
    """Factory method to create an instance from the config file."""
    with open('app/user_report/table_config.json') as json_file:
      return TableConfig(json.load(json_file))

  def get_activity_ids(self):
    """Returns a list of activity ids."""
    return [id for id in self._activity_id_to_canonical.keys()]

  def get_css(self):
    """Returns CSS generated from the json config."""
    css = []
    css.append(_COMMON_CSS)
    for class_name, color_code in self._colors.items():
      css.append('td.{0} {{ background-color: {1}; }}'.format(
          class_name, color_code))
    return '\n'.join(css)

  def _convert_to_canonical_id(self, score_map):
    """Returns a copy of score_map where keys are converted to canonical id."""
    result = {}
    for activity_id, score in score_map.items():
      canonical_id = self._activity_id_to_canonical.get(activity_id, None)
      if canonical_id is None:
        logging.error("Ignoring unknown activity_id: '{}'".format(activity_id))
        continue
      result[canonical_id] = score
    return result

  def make_html_table(self, score_map):
    """Returns an HTML table report for the given score_map."""
    canonical_score_map = self._convert_to_canonical_id(score_map)
    htmls = []
    htmls.append('<table>')
    for canonical_id, activity_msgs in self._list:
      score = canonical_score_map.get(canonical_id, None)
      text_and_color = self._get_message_from_score(canonical_id, score)
      htmls.append(html_table_line(activity_msgs.title, text_and_color))
    htmls.append('</table>')
    return ''.join(htmls)

  def _get_message_from_score(self, canonical_id, score):
    """Returns a TextAndColor object corresponding to (canonical_id, score)."""
    if score is None:
      return TextAndColor('Not completed', self._default_color)
    if canonical_id not in self._dict:
      return TextAndColor('Unknown survey', self._default_color)
    index = bisect_right(self._dict[canonical_id].ranges, score)
    if index > 0:
      index -= 1
    return self._dict[canonical_id].text_colors[index]

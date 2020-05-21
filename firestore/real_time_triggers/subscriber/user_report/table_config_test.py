# Lint as: python3
"""Tests for table_config."""

import json
import string
import unittest

from user_report.table_config import TableConfig

_CONFIG_JSON = """
{
  "questionnaires": [
    {"id": "SurveyA",
     "title": "Title A",
     "buckets": [
      {"lower_bound": 0, "text": "Zero", "color-ref": "green"},
      {"lower_bound": 1, "text": "One", "color-ref": "yellow"},
      {"lower_bound": 2, "text": "Two", "color-ref": "light-red"}
    ]},
    {"id": "SurveyB",
     "title": "Title B",
     "buckets": [
      {"lower_bound": 0, "text": "None or minimal", "color-ref": "green"},
      {"lower_bound": 5, "text": "Mild", "color-ref": "yellow"},
      {"lower_bound": 10, "text": "Moderate", "color-ref": "light-red"}
    ]}
  ],
  "colors": {
    "white": "#FFFFFF",
    "green": "#CEE0D4",
    "yellow": "#FFF2CC",
    "light-red": "#FBE5D6"
  },
  "default-color": "white"
}
"""

# Helper function to remove all spaces in HTML for comparison.
def format_html(html):
  return html.translate(str.maketrans('', '', string.whitespace))

class TableConfigTest(unittest.TestCase):
  def test_get_activity_ids(self):
    config = TableConfig(json.loads(_CONFIG_JSON))
    self.assertEqual(config.get_activity_ids(), ["SurveyA", "SurveyB"])
  def test_make_html_table(self):
    config = TableConfig(json.loads(_CONFIG_JSON))
    html_table = config.make_html_table({"SurveyA": 2, "SurveyB": 7})
    expected_html = """
    <table style="border:1px solid #999999;border-collapse: collapse;">
      <tr style="height:2.2rem;border:1px solid #999999;vertical-align:middle;">
        <td style="width:67%;border:1px solid #999999;">Title A</td>
        <td style="width:33%;background-color:#FBE5D6;text-align:center;">Two</td>
      </tr>
      <tr style="height:2.2rem;border:1px solid #999999;vertical-align:middle;">
        <td style="width:67%;border:1px solid #999999;">Title B</td>
        <td style="width:33%;background-color:#FFF2CC;text-align:center;">Mild</td>
      </tr>
    </table>"""
    self.assertEqual(format_html(html_table), format_html(expected_html))

  def test_make_html_table_survey_missing(self):
    config = TableConfig(json.loads(_CONFIG_JSON))
    html_table = config.make_html_table({"SurveyA": 2})
    expected_html = """
    <table style="border:1px solid #999999;border-collapse: collapse;">
      <tr style="height:2.2rem;border:1px solid #999999;vertical-align:middle;">
        <td style="width:67%;border:1px solid #999999;">Title A</td>
        <td style="width:33%;background-color:#FBE5D6;text-align:center;">Two</td>
      </tr>
      <tr style="height:2.2rem;border:1px solid #999999;vertical-align:middle;">
        <td style="width:67%;border:1px solid #999999;">Title B</td>
        <td style="width:33%;background-color:#FFFFFF;text-align:center;">Not completed</td>
      </tr>
    </table>"""
    self.assertEqual(format_html(html_table), format_html(expected_html))


if __name__ == '__main__':
  unittest.main()

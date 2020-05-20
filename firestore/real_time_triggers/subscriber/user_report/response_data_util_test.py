""" Tests for response_data_util.

Command to run tests:
  python3 -m unittest user_report/response_data_util_test.py -v
"""

import unittest

from user_report import response_data_util

class TestComputeScoreSum(unittest.TestCase):
  def test_valid_int(self):
    response_data = {'results': [
        {'key': 'Q1', 'value': '2'},
        {'key': 'Q2', 'value': '5'},
        {'key': 'Q3', 'value': '8'}]}
    self.assertEqual(response_data_util.compute_score_sum(response_data), 15)
  def test_valid_float(self):
    response_data = {'results': [
        {'key': 'Q1', 'value': '2.1'},
        {'key': 'Q2', 'value': '3.2'},
        {'key': 'Q3', 'value': '4.3'}]}
    self.assertAlmostEqual(response_data_util.compute_score_sum(response_data),
                           9.6)
  def test_ignore_negative(self):
    response_data = {'results': [
        {'key': 'Q1', 'value': '2'},
        {'key': 'Q2', 'value': '3'},
        {'key': 'Q3', 'value': '-999'}]}
    self.assertEqual(response_data_util.compute_score_sum(response_data), 5)
  def test_ignore_not_number(self):
    response_data = {'results': [
        {'key': 'Q1', 'value': '2'},
        {'key': 'Q2', 'value': '3'},
        {'key': 'Q3', 'value': 'NA'}]}
    self.assertEqual(response_data_util.compute_score_sum(response_data), 5)
  def test_ignore_dummy_sum_question(self):
    response_data = {'results': [
        {'key': 'Q1', 'value': '2'},
        {'key': 'Q2', 'value': '3'},
        {'key': '_SUM', 'value': '8'}]}
    self.assertEqual(response_data_util.compute_score_sum(response_data), 5)


if __name__ == '__main__':
  unittest.main()

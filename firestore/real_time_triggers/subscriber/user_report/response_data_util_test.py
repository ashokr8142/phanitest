""" Tests for response_data_util.

Command to run tests:
  python3 -m unittest user_report/response_data_util_test.py -v
"""

import datetime
import unittest

from user_report import response_data_util
from user_report.response_data_util import WeeklyResponses

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

class TestMakeScoreMap(unittest.TestCase):
  def test_surveys_unique(self):
    survey_a = {
        'activityId': 'SurveyA',
        'results': [{'key': 'Q1', 'value': '2'},
                    {'key': 'Q2', 'value': '5'},
                    {'key': 'Q3', 'value': '8'}]}
    survey_b = {
        'activityId': 'SurveyB',
        'results': [{'key': 'Q1', 'value': '1'},
                    {'key': 'Q2', 'value': '3'}]}
    survey_c = {
        'activityId': 'SurveyC',
        'results': [{'key': 'Q1', 'value': '1'},
                    {'key': 'Q2', 'value': '2'},
                    {'key': 'Q3', 'value': '3'},
                    {'key': 'Q4', 'value': '4'}]}
    survey_responses = [survey_a, survey_b, survey_c]
    self.assertEqual(response_data_util.make_score_map(survey_responses),
                     {'SurveyA': 15, 'SurveyB': 4, 'SurveyC': 10})
  def test_duplicated_activity_id_takes_last_survey(self):
    survey_a1 = {
        'activityId': 'SurveyA',
        'results': [{'key': 'Q1', 'value': '1'},
                    {'key': 'Q2', 'value': '2'},
                    {'key': 'Q3', 'value': '3'}]}
    survey_b = {
        'activityId': 'SurveyB',
        'results': [{'key': 'Q1', 'value': '1'},
                    {'key': 'Q2', 'value': '3'}]}
    survey_a2 = {
        'activityId': 'SurveyA',
        'results': [{'key': 'Q1', 'value': '2'},
                    {'key': 'Q3', 'value': '8'}]}
    survey_a3 = {
        'activityId': 'SurveyA',
        'results': [{'key': 'Q1', 'value': '3'},
                    {'key': 'Q2', 'value': '4'},
                    {'key': 'Q3', 'value': '5'}]}
    survey_responses = [survey_a1, survey_b, survey_a2, survey_a3]
    self.assertEqual(response_data_util.make_score_map(survey_responses),
                     {'SurveyA': 12, 'SurveyB': 4})


class TestGetCutoffTimestampForDate(unittest.TestCase):
  def test_daylight_saving(self):
    date = datetime.date(2020, 5, 17)
    self.assertEqual(response_data_util.get_cutoff_timestamp_for_date(date),
                     1589731200000)
  def test_non_daylight_saving(self):
    date = datetime.date(2020, 11, 1)
    self.assertEqual(response_data_util.get_cutoff_timestamp_for_date(date),
                     1604250000000)


class TestGroupByWeek(unittest.TestCase):
  def test_no_empty_week(self):
    current_week_start_date = datetime.date(2020, 5, 17)
    s1 = {
        'activityId': 'SurveyA',
        'createdTimestamp': '1589119200000', # 2020-05-10 10am ET
        'results': [{'key': 'Q1', 'value': '7'}, {'key': 'Q2', 'value': '8'}]}
    s2 = {
        'activityId': 'SurveyB',
        'createdTimestamp': '1589565600000', # 2020-05-15 2pm ET
        'results': [{'key': 'Q1', 'value': '5'}, {'key': 'Q2', 'value': '6'}]}
    s3 = {
        'activityId': 'SurveyA',
        'createdTimestamp': '1589727600000', # 2020-05-17 11am ET
        'results': [{'key': 'Q1', 'value': '3'}, {'key': 'Q2', 'value': '4'}]}
    s4 = {
        'activityId': 'SurveyA',
        'createdTimestamp': '1589986800000', # 2020-05-20 11am ET
        'results': [{'key': 'Q1', 'value': '1'}, {'key': 'Q2', 'value': '2'}]}
    survey_responses = [s1, s2, s3, s4]
    expected_result = [
        WeeklyResponses(datetime.date(2020, 5, 3), [s1]),
        WeeklyResponses(datetime.date(2020, 5, 10), [s2, s3]),
        WeeklyResponses(datetime.date(2020, 5, 17), [s4])]
    self.assertEqual(response_data_util.group_by_week(survey_responses,
                                                      current_week_start_date),
                     expected_result)
  def test_current_week_empty(self):
    current_week_start_date = datetime.date(2020, 5, 24)
    s1 = {
        'activityId': 'SurveyA',
        'createdTimestamp': '1589119200000', # 2020-05-10 10am ET
        'results': [{'key': 'Q1', 'value': '7'}, {'key': 'Q2', 'value': '8'}]}
    s2 = {
        'activityId': 'SurveyB',
        'createdTimestamp': '1589565600000', # 2020-05-15 2pm ET
        'results': [{'key': 'Q1', 'value': '5'}, {'key': 'Q2', 'value': '6'}]}
    s3 = {
        'activityId': 'SurveyA',
        'createdTimestamp': '1589727600000', # 2020-05-17 11am ET
        'results': [{'key': 'Q1', 'value': '3'}, {'key': 'Q2', 'value': '4'}]}
    s4 = {
        'activityId': 'SurveyA',
        'createdTimestamp': '1589986800000', # 2020-05-20 11am ET
        'results': [{'key': 'Q1', 'value': '1'}, {'key': 'Q2', 'value': '2'}]}
    survey_responses = [s1, s2, s3, s4]
    expected_result = [
        WeeklyResponses(datetime.date(2020, 5, 3), [s1]),
        WeeklyResponses(datetime.date(2020, 5, 10), [s2, s3]),
        WeeklyResponses(datetime.date(2020, 5, 17), [s4]),
        WeeklyResponses(datetime.date(2020, 5, 24), [])]
    self.assertEqual(response_data_util.group_by_week(survey_responses,
                                                      current_week_start_date),
                     expected_result)

  def test_skip_two_weeks(self):
    current_week_start_date = datetime.date(2020, 5, 24)
    s1 = {
        'activityId': 'SurveyA',
        'createdTimestamp': '1589119200000', # 2020-05-10 10am ET
        'results': [{'key': 'Q1', 'value': '7'}, {'key': 'Q2', 'value': '8'}]}
    s2 = {
        'activityId': 'SurveyA',
        'createdTimestamp': '1590339600000', # 2020-05-24 1pm ET
        'results': [{'key': 'Q1', 'value': '1'}, {'key': 'Q2', 'value': '2'}]}
    s3 = {
        'activityId': 'SurveyB',
        'createdTimestamp': '1590343200000', # 2020-05-24 2pm ET
        'results': [{'key': 'Q1', 'value': '3'}, {'key': 'Q2', 'value': '4'}]}
    survey_responses = [s1, s2, s3]
    expected_result = [
        WeeklyResponses(datetime.date(2020, 5, 3), [s1]),
        WeeklyResponses(datetime.date(2020, 5, 10), []),
        WeeklyResponses(datetime.date(2020, 5, 17), []),
        WeeklyResponses(datetime.date(2020, 5, 24), [s2, s3])]
    self.assertEqual(response_data_util.group_by_week(survey_responses,
                                                      current_week_start_date),
                     expected_result)


if __name__ == '__main__':
  unittest.main()

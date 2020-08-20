package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.bean.ParticipantChartActivity;
import com.google.cloud.healthcare.fdamystudies.bean.ParticipantChartBean;
import com.google.cloud.healthcare.fdamystudies.config.ApplicationConfiguration;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantChartInfoBo;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantChartInfoBoRepository;
import com.google.cloud.healthcare.fdamystudies.utils.Activity;
import com.google.cloud.healthcare.fdamystudies.utils.AppConstants;
import com.google.cloud.healthcare.fdamystudies.utils.AppUtil;
import com.google.cloud.healthcare.fdamystudies.utils.Buckets;
import com.google.cloud.healthcare.fdamystudies.utils.ProcessResponseException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipantChartInfoServiceImpl implements ParticipantChartInfoService {

  @Autowired ParticipantChartInfoBoRepository participantChartInfoBoRepository;
  @Autowired ApplicationConfiguration appConfig;
  private static final Logger logger =
      LoggerFactory.getLogger(ParticipantChartInfoServiceImpl.class);

  @Override
  public List<ParticipantChartBean> getParticipantChartBean(String participantId, String studyId)
      throws IOException, ProcessResponseException {
    Map<String, Activity> activityChartConfigMap = AppUtil.getChartJsonConfig();
    if (activityChartConfigMap.isEmpty()) {
      throw new ProcessResponseException("Could not load the activity config json for chart");
    }
    LocalDateTime dateTimeMin = null;
    LocalDateTime dateTimeMax = null;
    List<ParticipantChartBean> participantChartBeanList = new ArrayList<ParticipantChartBean>();
    try {
      dateTimeMin =
          participantChartInfoBoRepository.findMinCreatedDateForParticipant(participantId, studyId);
      dateTimeMax =
          participantChartInfoBoRepository.findMaxCreatedDateForParticipant(participantId, studyId);
      // Create dynamic quarter timeframes
    } catch (Exception ex) {
      logger.error(
          "Error retrieving participant's min/max date for chart info. \n Error is "
              + ex.getMessage());
      throw new ProcessResponseException(
          "Could not retrieve participant's min/max date for chart info. \\n Error is \"\r\n"
              + " ex.getMessage()");
    }
    if (dateTimeMin == null || dateTimeMax == null) {
      return participantChartBeanList;
    }

    LocalDateTime dateTimeStart = dateTimeMin.toLocalDate().withDayOfMonth(1).atTime(LocalTime.MIN);
    LocalDateTime dateTimeEnd = dateTimeStart.plusMonths(3).minusDays(1).with(LocalTime.MAX);

    while (dateTimeStart.compareTo(dateTimeMax) <= 0) {
      ParticipantChartBean tempParticipantChartBean = new ParticipantChartBean();
      String monthRangeString =
          dateTimeStart.getMonth()
              + AppConstants.SPACE_STR
              + dateTimeStart.getYear()
              + AppConstants.SPACE_STR
              + AppConstants.HYPHEN
              + AppConstants.SPACE_STR
              + dateTimeEnd.getMonth()
              + AppConstants.SPACE_STR
              + dateTimeEnd.getYear();

      tempParticipantChartBean.setMonthRange(monthRangeString);
      tempParticipantChartBean.setChartActivityList(new ArrayList<ParticipantChartActivity>());
      for (String activityId : AppConstants.getActivityIdsToProcessForChart()) {
        try {
          ParticipantChartActivity tempBean =
              createChartActivityData(
                  participantId,
                  studyId,
                  activityId,
                  dateTimeStart,
                  dateTimeEnd,
                  activityChartConfigMap);
          if (tempBean != null) {
            tempParticipantChartBean.getChartActivityList().add(tempBean);
          }
        } catch (Exception e) {
          logger.error(
              "Could not create chart for activityId: "
                  + activityId
                  + "\n Error is: "
                  + e.getMessage());
        }
      }
      participantChartBeanList.add(tempParticipantChartBean);
      dateTimeStart = dateTimeEnd.plusDays(1).with(LocalTime.MIN);
      dateTimeEnd = dateTimeStart.plusMonths(3).minusDays(1).with(LocalTime.MAX);
    }

    return participantChartBeanList;
  }

  private ParticipantChartActivity createChartActivityData(
      String participantId,
      String studyId,
      String activityId,
      LocalDateTime dateTimeStart,
      LocalDateTime dateTimeEnd,
      Map<String, Activity> activityChartConfigMap) {
    try {
      List<ParticipantChartInfoBo> participantChartInfoBoList =
          participantChartInfoBoRepository.findParticipantChartInfoBetweenDates(
              participantId, studyId, activityId, dateTimeStart, dateTimeEnd);
      return createParticipantChartActivityList(
          activityId, participantChartInfoBoList, activityChartConfigMap);

    } catch (Exception e) {
      logger.error(
          "createChartActivityData() - Could not create chart for activityId: "
              + activityId
              + "\n Error is: "
              + e.getMessage());
      return null;
    }
  }

  private ParticipantChartActivity createParticipantChartActivityList(
      String activityId,
      List<ParticipantChartInfoBo> participantChartInfoBoList,
      Map<String, Activity> activityChartConfigMap)
      throws ProcessResponseException {
    ParticipantChartActivity retParticipantChartActivity = new ParticipantChartActivity();
    retParticipantChartActivity.setActivityId(activityId);
    if (participantChartInfoBoList.isEmpty()) {
      retParticipantChartActivity.setActivityIdIndex(activityId + "999");
    }
    Activity activityConfig = getActivityConfig(activityId, activityChartConfigMap);
    if (activityConfig == null) {
      throw new ProcessResponseException(
          "Could not load the activity config for activity Id, for chart");
    }
    retParticipantChartActivity.setActivityIdTitle(activityConfig.getTitle());
    List<String> labelsList = new ArrayList<String>();
    List<Integer> dataList = new ArrayList<Integer>();
    List<String> axisLabelsList = getAxisLabelsForBucket(activityConfig);
    int activityIndex = 0;
    for (ParticipantChartInfoBo tempParticipantChartInfoBo : participantChartInfoBoList) {
      try {
        activityIndex++;
        retParticipantChartActivity.setActivityIdIndex(activityId + activityIndex);

        double questionResponseValue =
            Double.parseDouble(tempParticipantChartInfoBo.getQuestionResponse());
        Buckets activityBucket = getNormalizedValues(questionResponseValue, activityConfig);
        if (activityBucket != null) {
          LocalDateTime dateTimeStartTmp = tempParticipantChartInfoBo.getCreated();
          labelsList.add(
              String.valueOf(dateTimeStartTmp.getDayOfMonth())
                  + AppConstants.SPACE_STR
                  + dateTimeStartTmp.getMonth().toString().substring(0, 3));
          dataList.add(activityBucket.getNormalizedValue());
        }

      } catch (NumberFormatException | NullPointerException ex) {
        // Do nothing - as this may be a valid null or non-numeric response
      }
    }
    retParticipantChartActivity.setLabels(labelsList);
    retParticipantChartActivity.setData(dataList);
    retParticipantChartActivity.setAxisLabels(axisLabelsList);
    return retParticipantChartActivity;
  }

  private Activity getActivityConfig(
      String activityId, Map<String, Activity> activityChartConfigMap) {

    for (String key : activityChartConfigMap.keySet()) {
      if (key.contains(activityId)) {
        return activityChartConfigMap.get(key);
      }
    }
    return null;
  }

  private Buckets getNormalizedValues(Double scoreValue, Activity activityConfig) {
    List<Buckets> activityBuckets = activityConfig.getBuckets();
    if (scoreValue == null || activityBuckets == null) {
      return null;
    }
    int score = scoreValue.intValue();
    for (int i = 0; i < activityBuckets.size(); i++) {
      Buckets tempBucket = activityBuckets.get(i);
      if (tempBucket != null) {
        if (score >= tempBucket.getLowerBound() && score <= tempBucket.getUpperBound()) {
          return tempBucket;
        }
      }
    }
    return null;
  }

  private List<String> getAxisLabelsForBucket(Activity activityConfig) {
    List<String> axisLabels = new ArrayList<String>();
    for (Buckets tempBucket : activityConfig.getBuckets()) {
      axisLabels.add(tempBucket.getText());
    }
    return axisLabels;
  }
}

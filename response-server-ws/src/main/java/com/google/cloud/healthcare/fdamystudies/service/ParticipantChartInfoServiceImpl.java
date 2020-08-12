package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.config.ApplicationConfiguration;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantChartInfoBo;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantChartInfoBoRepository;
import com.google.cloud.healthcare.fdamystudies.utils.Activity;
import com.google.cloud.healthcare.fdamystudies.utils.AppConstants;
import com.google.cloud.healthcare.fdamystudies.utils.AppUtil;
import com.google.cloud.healthcare.fdamystudies.utils.Buckets;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipantChartInfoServiceImpl implements ParticipantChartInfoService {

  @Autowired ParticipantChartInfoBoRepository participantChartInfoBoRepository;
  @Autowired ApplicationConfiguration appConfig;

  @Override
  public String getParticpantChartAsHtml(String participantId, String studyId) throws IOException {
    StringBuilder htmlChartContent = new StringBuilder();
    htmlChartContent.append(AppConstants.CHART_HTML_STR_START);
    LocalDateTime dateTimeMin =
        participantChartInfoBoRepository.findMinCreatedDateForParticipant(participantId, studyId);
    LocalDateTime dateTimeStart = dateTimeMin.toLocalDate().withDayOfMonth(1).atTime(LocalTime.MIN);
    LocalDateTime dateTimeEnd = dateTimeStart.plusMonths(3).minusDays(1).with(LocalTime.MAX);
    StringBuilder htmlChartContentDynamic = new StringBuilder();
    Map<String, Activity> activityChartConfigMap = AppUtil.getChartJsonConfig();
    if (activityChartConfigMap.isEmpty()) {
      htmlChartContentDynamic.append(AppConstants.NO_CHART_DISPLAY_INFORMATION);
    } else {
      String sleepChartSnippet =
          getChartHtmlSnippet(
              participantId,
              studyId,
              AppConstants.PSQIPUBLIC1_ACTIVITY_ID,
              dateTimeStart,
              dateTimeEnd,
              htmlChartContent,
              activityChartConfigMap);
      if (!sleepChartSnippet.isEmpty()) {
        htmlChartContentDynamic.append(sleepChartSnippet);
      }
      String depressionChartSnippet =
          getChartHtmlSnippet(
              participantId,
              studyId,
              AppConstants.PHQ9PUBLIC_ACTIVITY_ID,
              dateTimeStart,
              dateTimeEnd,
              htmlChartContent,
              activityChartConfigMap);
      if (!depressionChartSnippet.isEmpty()) {
        htmlChartContentDynamic.append(depressionChartSnippet);
      }
      String anxietyChartSnippet =
          getChartHtmlSnippet(
              participantId,
              studyId,
              AppConstants.GAD7PUBLIC1_ACTIVITY_ID,
              dateTimeStart,
              dateTimeEnd,
              htmlChartContent,
              activityChartConfigMap);
      if (!anxietyChartSnippet.isEmpty()) {
        htmlChartContentDynamic.append(anxietyChartSnippet);
      }
      String ptsdChartSnippet =
          getChartHtmlSnippet(
              participantId,
              studyId,
              AppConstants.PTSDPUBLIC_ACTIVITY_ID,
              dateTimeStart,
              dateTimeEnd,
              htmlChartContent,
              activityChartConfigMap);
      if (!ptsdChartSnippet.isEmpty()) {
        htmlChartContentDynamic.append(ptsdChartSnippet);
      }
      String mentalHealthChartSnippet =
          getChartHtmlSnippet(
              participantId,
              studyId,
              AppConstants.WSASPUBLIC_ACTIVITY_ID,
              dateTimeStart,
              dateTimeEnd,
              htmlChartContent,
              activityChartConfigMap);
      if (!mentalHealthChartSnippet.isEmpty()) {
        htmlChartContentDynamic.append(mentalHealthChartSnippet);
      }
    }
    htmlChartContent.append(AppConstants.CHART_HTML_CHART_PREFIX_SCRIPT);
    htmlChartContent.append(htmlChartContentDynamic.toString());
    htmlChartContent.append(AppConstants.CHART_HTML_STR_END);
    return htmlChartContent.toString();
  }

  private String getChartHtmlSnippet(
      String participantId,
      String studyId,
      String activityId,
      LocalDateTime dateTimeStart,
      LocalDateTime dateTimeEnd,
      StringBuilder htmlChartContent,
      Map<String, Activity> activityChartConfigMap) {
    if (activityChartConfigMap.isEmpty()) {

      return AppConstants.NO_CHART_DISPLAY_INFORMATION;
    }
    List<ParticipantChartInfoBo> participantChartInfoBoPsPqList =
        participantChartInfoBoRepository
            .findByParticipantIdentifierAndStudyIdAndActivityIdOrderByCreatedAsc(
                participantId, studyId, activityId);

    if (!participantChartInfoBoPsPqList.isEmpty()) {
      Activity activityConfig = getActivityConfig(activityId, activityChartConfigMap);

      if (activityConfig == null) {
        return AppConstants.NO_CHART_DISPLAY_INFORMATION;
      }
      buildHtmlChartContainerSnippet(
          activityId, activityConfig.getTitle(), dateTimeStart, dateTimeEnd, htmlChartContent);
      Map<String, Double> chartInfoMap = new LinkedHashMap<String, Double>();
      for (ParticipantChartInfoBo partChartBo : participantChartInfoBoPsPqList) {

        if (partChartBo.getCreated().isBefore(dateTimeEnd)) {
          try {
            double questionResponseValue = Double.parseDouble(partChartBo.getQuestionResponse());
            chartInfoMap.put(String.valueOf(partChartBo.getCreated()), questionResponseValue);
          } catch (NumberFormatException | NullPointerException ex) {
            // Do nothing - as this may be a valid null or non-numeric response
          }
        }
      }
      return buildLabelDataHtmlSnippet(chartInfoMap, activityId, activityConfig);
    }

    return AppConstants.EMPTY_STR;
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

  private void buildHtmlChartContainerSnippet(
      String activityId,
      String activityIdLabel,
      LocalDateTime dateTimeStart,
      LocalDateTime dateTimeEnd,
      StringBuilder htmlChartContent) {
    if (!activityId.isEmpty()) {
      if (!activityIdLabel.isEmpty()) {
        String monthRangeString =
            dateTimeStart.getMonth() + AppConstants.HYPHEN + dateTimeEnd.getMonth();
        String chartLabelContainerSnippetWithActivityId =
            MessageFormat.format(
                AppConstants.CHART_HTML_STR_CONTAINER_DYNAMIC,
                activityIdLabel,
                monthRangeString,
                activityId);
        htmlChartContent.append(chartLabelContainerSnippetWithActivityId);
      }
    }
  }

  private String buildLabelDataHtmlSnippet(
      Map<String, Double> chartInfoMap, String activityId, Activity activityConfig) {
    StringBuilder tempStringBuilderLabel = new StringBuilder();
    String chartLabelSnippetWithActivityId =
        MessageFormat.format(AppConstants.CHART_HTML_CHART_PREFIX_1, activityId);

    tempStringBuilderLabel.append(chartLabelSnippetWithActivityId);
    tempStringBuilderLabel.append(AppConstants.SQUARE_BRACKET_OPEN);
    StringBuilder tempStringBuilderData = new StringBuilder();
    String chartDataSnippetWithActivityId =
        MessageFormat.format(AppConstants.CHART_HTML_CHART_PREFIX_2, activityId);
    tempStringBuilderData.append(chartDataSnippetWithActivityId);
    tempStringBuilderData.append(AppConstants.SQUARE_BRACKET_OPEN);
    List<String> axisLabels = new ArrayList<String>();
    for (Map.Entry<String, Double> entry : chartInfoMap.entrySet()) {
      try {
        LocalDateTime dateTimeStartTmp = LocalDateTime.parse(entry.getKey());
        tempStringBuilderLabel.append(
            "'"
                + String.valueOf(dateTimeStartTmp.getDayOfMonth())
                + AppConstants.SPACE_STR
                + dateTimeStartTmp.getMonth().toString().substring(0, 3)
                + "'");
        tempStringBuilderLabel.append(AppConstants.COMMA_STR);
        Buckets activityBucket = getNormalizedValues(entry.getValue(), activityConfig);
        if (activityBucket != null) {
          tempStringBuilderData.append(activityBucket.getNormalizedValue());
          axisLabels.add(activityBucket.getText());
        }
        tempStringBuilderData.append(AppConstants.COMMA_STR);
      } catch (DateTimeParseException de) {
        // Do nothing - skip this entry for char
      }
    }

    tempStringBuilderLabel.append(AppConstants.SQUARE_BRACKET_CLOSE);
    tempStringBuilderLabel.append(AppConstants.SEMI_COLON);

    tempStringBuilderData.append(AppConstants.SQUARE_BRACKET_CLOSE);
    tempStringBuilderData.append(AppConstants.SEMI_COLON);
    String chartBodySnippetWithActivityId =
        MessageFormat.format(AppConstants.CHART_HTML_CHART_DYNAMIC, activityId);
    String axisLabelsString = getAxisLabelsHtmlString(activityConfig.getBuckets());
    return tempStringBuilderLabel.toString()
        + tempStringBuilderData.toString()
        + chartBodySnippetWithActivityId
        + AppConstants.CHART_HTML_CHART_STATIC_1
        + axisLabelsString
        + AppConstants.CHART_HTML_CHART_STATIC_2;
  }

  private String getAxisLabelsHtmlString(List<Buckets> activityBucketsList) {

    if (activityBucketsList.isEmpty()) {
      return AppConstants.EMPTY_STR;
    }
    int listSize = activityBucketsList.size();

    if (listSize == 1) {
      return MessageFormat.format(
          AppConstants.CHART_HTML_CHART_AXIS_LABELS_1, activityBucketsList.get(0).getText());
    } else if (listSize == 2) {
      return MessageFormat.format(
          AppConstants.CHART_HTML_CHART_AXIS_LABELS_2,
          activityBucketsList.get(0).getText(),
          activityBucketsList.get(1).getText());
    } else if (listSize == 3) {
      return MessageFormat.format(
          AppConstants.CHART_HTML_CHART_AXIS_LABELS_3,
          activityBucketsList.get(0).getText(),
          activityBucketsList.get(1).getText(),
          activityBucketsList.get(2).getText());
    } else if (listSize == 4) {
      return MessageFormat.format(
          AppConstants.CHART_HTML_CHART_AXIS_LABELS_4,
          activityBucketsList.get(0).getText(),
          activityBucketsList.get(1).getText(),
          activityBucketsList.get(2).getText(),
          activityBucketsList.get(3).getText());
    } else if (listSize == 5) {
      return MessageFormat.format(
          AppConstants.CHART_HTML_CHART_AXIS_LABELS_5,
          activityBucketsList.get(0).getText(),
          activityBucketsList.get(1).getText(),
          activityBucketsList.get(2).getText(),
          activityBucketsList.get(3).getText(),
          activityBucketsList.get(4).getText());
    } else {
      return AppConstants.EMPTY_STR;
    }
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
}

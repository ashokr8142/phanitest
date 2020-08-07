package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.config.ApplicationConfiguration;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantChartInfoBo;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantChartInfoBoRepository;
import com.google.cloud.healthcare.fdamystudies.utils.AppConstants;
import java.text.MessageFormat;
import java.time.LocalDateTime;
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
  public String getParticpantChartAsHtml(String participantId, String studyId) {
    StringBuilder htmlChartContent = new StringBuilder();
    htmlChartContent.append(AppConstants.CHART_HTML_STR_START);
    LocalDateTime dateTimeStart = LocalDateTime.parse(appConfig.getChartDateStart());
    LocalDateTime dateTimeEnd = dateTimeStart.plusMonths(3);
    String sleepChartSnippet =
        getChartHtmlSnippet(
            participantId, studyId, AppConstants.PSQIPUBLIC1_ACTIVITY_ID, dateTimeEnd);
    if (!sleepChartSnippet.isEmpty()) {
      htmlChartContent.append(sleepChartSnippet);
    }
    String depressionChartSnippet =
        getChartHtmlSnippet(
            participantId, studyId, AppConstants.PHQ9PUBLIC_ACTIVITY_ID, dateTimeEnd);
    if (!depressionChartSnippet.isEmpty()) {
      htmlChartContent.append(depressionChartSnippet);
    }
    String anxietyChartSnippet =
        getChartHtmlSnippet(
            participantId, studyId, AppConstants.GAD7PUBLIC1_ACTIVITY_ID, dateTimeEnd);
    if (!anxietyChartSnippet.isEmpty()) {
      htmlChartContent.append(anxietyChartSnippet);
    }
    String ptsdChartSnippet =
        getChartHtmlSnippet(
            participantId, studyId, AppConstants.PTSDPUBLIC_ACTIVITY_ID, dateTimeEnd);
    if (!ptsdChartSnippet.isEmpty()) {
      htmlChartContent.append(ptsdChartSnippet);
    }
    String mentalHealthChartSnippet =
        getChartHtmlSnippet(
            participantId, studyId, AppConstants.WSASPUBLIC_ACTIVITY_ID, dateTimeEnd);
    if (!mentalHealthChartSnippet.isEmpty()) {
      htmlChartContent.append(mentalHealthChartSnippet);
    }
    htmlChartContent.append(AppConstants.CHART_HTML_STR_END);
    return htmlChartContent.toString();
  }

  private String getChartHtmlSnippet(
      String participantId, String studyId, String activityId, LocalDateTime dateTimeEnd) {
    List<ParticipantChartInfoBo> participantChartInfoBoPsPqList =
        participantChartInfoBoRepository
            .findByParticipantIdentifierAndStudyIdAndActivityIdOrderByCreatedAsc(
                participantId, studyId, activityId);

    if (!participantChartInfoBoPsPqList.isEmpty()) {
      Map<String, Integer> chartInfoMap = new LinkedHashMap<String, Integer>();
      for (ParticipantChartInfoBo partChartBo : participantChartInfoBoPsPqList) {

        if (partChartBo.getCreated().isBefore(dateTimeEnd)) {
          try {
            int questionResponseValue = Integer.parseInt(partChartBo.getQuestionResponse());
            chartInfoMap.put(
                String.valueOf(partChartBo.getCreated().getDayOfMonth()), questionResponseValue);
          } catch (NumberFormatException | NullPointerException ex) {
            // Do nothing - as this may be a valid null or non-numeric response
          }
        }
      }
      return buildLabelDataHtmlSnippet(chartInfoMap, activityId);
    }
    return AppConstants.EMPTY_STR;
  }

  private String buildLabelDataHtmlSnippet(Map<String, Integer> chartInfoMap, String activityId) {
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
    for (Map.Entry<String, Integer> entry : chartInfoMap.entrySet()) {
      tempStringBuilderLabel.append(entry.getKey());
      tempStringBuilderLabel.append(AppConstants.COMMA_STR);
      tempStringBuilderData.append(entry.getValue());
      tempStringBuilderData.append(AppConstants.COMMA_STR);
    }

    tempStringBuilderLabel.append(AppConstants.SQUARE_BRACKET_CLOSE);
    tempStringBuilderLabel.append(AppConstants.SEMI_COLON);

    tempStringBuilderData.append(AppConstants.SQUARE_BRACKET_CLOSE);
    tempStringBuilderData.append(AppConstants.SEMI_COLON);
    String chartBodySnippetWithActivityId =
        MessageFormat.format(AppConstants.CHART_HTML_CHART_DYNAMIC, activityId);

    return tempStringBuilderLabel.toString()
        + tempStringBuilderData.toString()
        + chartBodySnippetWithActivityId
        + AppConstants.CHART_HTML_CHART_STATIC;
  }
}

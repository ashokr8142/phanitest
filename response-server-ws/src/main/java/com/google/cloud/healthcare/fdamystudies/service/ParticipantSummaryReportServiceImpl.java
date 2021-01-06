package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.bean.SummaryReportBean;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantChartInfoBo;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantChartInfoBoRepository;
import com.google.cloud.healthcare.fdamystudies.utils.AppConstants;
import com.google.cloud.healthcare.fdamystudies.utils.AppUtil;
import com.google.cloud.healthcare.fdamystudies.utils.SummaryReportUtil;
import com.google.cloud.healthcare.fdamystudies.utils.TableConfigUtil;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipantSummaryReportServiceImpl implements ParticipantSummaryReportService {

  private static final Logger logger =
      LoggerFactory.getLogger(ParticipantSummaryReportServiceImpl.class);

  @Autowired private ParticipantChartInfoBoRepository participantChartInfoBoRepository;
  @Autowired private SummaryReportUtil summaryReportUtil;

  private List<SummaryReportBean> generateSummaryReports(String participantId) {
    logger.info("ParticipantSummaryReportServiceImpl generateSummaryReports() - starts ");
    List<SummaryReportBean> summaryReportBeanList = new ArrayList<>();
    try {
      Map<String, List<ParticipantChartInfoBo>> surveyResponsesGroupedByWeek =
          getSurveyResponsesGroupedByWeek(participantId);

      // current week report
      if (!surveyResponsesGroupedByWeek.isEmpty()) {
        String currentWeek = null;
        for (Map.Entry<String, List<ParticipantChartInfoBo>> surveyResponse :
            surveyResponsesGroupedByWeek.entrySet()) {
          currentWeek = surveyResponse.getKey();
          break;
        }

        SummaryReportBean currentWeekReport = new SummaryReportBean();
        currentWeekReport.setTitle(AppConstants.CURRENT_WEEK_REPORT_TEXT + currentWeek);
        currentWeekReport.setContent(
            generateCurrentWeekContent(
                    getCurrentWeekActivityScoreMap(surveyResponsesGroupedByWeek.get(currentWeek)))
                .toString());
        summaryReportBeanList.add(currentWeekReport);
        // remove the current week response so only past responses will be available
        surveyResponsesGroupedByWeek.remove(currentWeek);
      }

      // past week reports
      if (!surveyResponsesGroupedByWeek.isEmpty()) {
        SummaryReportBean pastWeekReport = new SummaryReportBean();
        pastWeekReport.setTitle(AppConstants.PAST_WEEK_REPORT_TEXT);
        pastWeekReport.setContent(
            generatePastWeekContent(getPastWeekActivityScoreMap(surveyResponsesGroupedByWeek))
                .toString());
        summaryReportBeanList.add(pastWeekReport);
      }
    } catch (Exception e) {
      logger.error("ParticipantSummaryReportServiceImpl generateSummaryReports() - error ", e);
    }
    logger.info("ParticipantSummaryReportServiceImpl generateSummaryReports() - Ends ");
    return summaryReportBeanList;
  }

  private Map<String, List<ParticipantChartInfoBo>> getSurveyResponsesGroupedByWeek(
      String participantId) {
    logger.info("ParticipantSummaryReportServiceImpl getSurveyResponsesGroupedByWeek() - starts ");

    Map<String, List<ParticipantChartInfoBo>> surveyResponsesGroupedByWeek =
        new LinkedHashMap<String, List<ParticipantChartInfoBo>>();
    try {

      int startWeek;
      int finishWeek;
      int diff;
      SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.DATE_FORMAT_yyyy_MM_dd);
      Calendar cal;
      Date inputDate;
      Date finishDate = new Date();
      String WeekStartDate = "";
      String WeekEndDate = "";
      SimpleDateFormat formater = new SimpleDateFormat(AppConstants.DATE_FORMAT_MMMM_dd_yyyy);
      DateTimeFormatter dateTimeFormatter =
          DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_yyyy_MM_dd);

      // get inputs from ParticipantChartInfo
      List<ParticipantChartInfoBo> participantChartInfoBoList =
          participantChartInfoBoRepository.findParticipantChartInfo(participantId);

      LocalDateTime startDateText = participantChartInfoBoList.get(0).getCreated();
      String formatedDate = dateTimeFormatter.format(startDateText);

      List<ParticipantChartInfoBo> participantChartListGroupedByWeek = null;

      inputDate = sdf.parse(formatedDate);

      cal = Calendar.getInstance();
      cal.setTime(inputDate);
      startWeek = cal.get(Calendar.WEEK_OF_YEAR);

      int totalNumberOfWeeks = cal.getWeeksInWeekYear();
      cal.setTime(finishDate);
      finishWeek = cal.get(Calendar.WEEK_OF_YEAR);

      if (finishWeek > startWeek) {
        diff = Math.abs(finishWeek - startWeek) + 1;
      } else {
        int weekDiff = totalNumberOfWeeks - startWeek;
        diff = weekDiff + finishWeek + 1;
      }

      cal = Calendar.getInstance();
      cal.setTime(finishDate);

      int getDayOfTheWeek = 0;
      int dayOfTheWeek = cal.get(Calendar.DAY_OF_WEEK);

      if (dayOfTheWeek == 1) {

        getDayOfTheWeek = 1;
        WeekStartDate = sdf.format(cal.getTime());

        LocalDate plusDate = LocalDate.parse(WeekStartDate);
        LocalDate returnWeekEndValue = plusDate.plusDays(6);

        WeekEndDate = returnWeekEndValue.toString();

      } else {
        getDayOfTheWeek = dayOfTheWeek - 1;
        LocalDate minusDate =
            LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId()).toLocalDate();
        LocalDate returnWeekStartValue = minusDate.minusDays(getDayOfTheWeek);

        LocalDate plusDate = LocalDate.parse(returnWeekStartValue.toString());
        LocalDate returnWeekEndValue = plusDate.plusDays(6);

        WeekStartDate = returnWeekStartValue.toString();
        WeekEndDate = returnWeekEndValue.toString();
      }

      for (int i = 0; i < diff; i++) {

        Date startDate =
            new SimpleDateFormat(AppConstants.DATE_FORMAT_yyyy_MM_dd).parse(WeekStartDate);
        Date endDate = new SimpleDateFormat(AppConstants.DATE_FORMAT_yyyy_MM_dd).parse(WeekEndDate);

        participantChartListGroupedByWeek = new ArrayList<ParticipantChartInfoBo>();
        for (ParticipantChartInfoBo participantChartInfo : participantChartInfoBoList) {

          Date actualDate =
              new SimpleDateFormat(AppConstants.DATE_FORMAT_yyyy_MM_dd)
                  .parse(dateTimeFormatter.format(participantChartInfo.getCreated()));

          if ((actualDate.equals(startDate) || actualDate.after(startDate))
              && (actualDate.equals(endDate) || actualDate.before(endDate))) {
            participantChartListGroupedByWeek.add(participantChartInfo);

            boolean isCurrentWeek = SummaryReportUtil.isDateInCurrentWeek(startDate);
            if (isCurrentWeek) {
              surveyResponsesGroupedByWeek.put(
                  formater.format(startDate), participantChartListGroupedByWeek);
            } else {
              surveyResponsesGroupedByWeek.put(
                  formater.format(startDate), participantChartListGroupedByWeek);
            }
          }
        }

        if (participantChartListGroupedByWeek.isEmpty()) {
          boolean isCurrentWeek = SummaryReportUtil.isDateInCurrentWeek(startDate);
          if (isCurrentWeek) {
            surveyResponsesGroupedByWeek.put(
                formater.format(startDate), participantChartListGroupedByWeek);
          } else {
            surveyResponsesGroupedByWeek.put(
                formater.format(startDate), participantChartListGroupedByWeek);
          }
        }

        LocalDate minusStartDate = LocalDate.parse(WeekStartDate);
        LocalDate returnWeekStartValue = minusStartDate.minusDays(7);

        LocalDate minusEndDate = LocalDate.parse(WeekStartDate);
        LocalDate returnWeekEndValue = minusEndDate.minusDays(1);

        WeekStartDate = returnWeekStartValue.toString();
        WeekEndDate = returnWeekEndValue.toString();
      }

    } catch (Exception e) {
      logger.error(
          "ParticipantSummaryReportServiceImpl getSurveyResponsesGroupedByWeek() - error ", e);
    }
    logger.info("ParticipantSummaryReportServiceImpl getSurveyResponsesGroupedByWeek() - Ends ");
    return surveyResponsesGroupedByWeek;
  }

  private Map<String, String> getCurrentWeekActivityScoreMap(
      List<ParticipantChartInfoBo> currentWeekSurveyReportList) {
    Map<String, String> activityScoreMap = new HashMap<>();
    for (ParticipantChartInfoBo participantChartInfo : currentWeekSurveyReportList) {
      activityScoreMap.put(
          participantChartInfo.getActivityId(), participantChartInfo.getQuestionResponse());
    }
    return activityScoreMap;
  }

  private Map<String, Map<String, String>> getPastWeekActivityScoreMap(
      Map<String, List<ParticipantChartInfoBo>> surveyResponsesGroupedByWeek) {
    LinkedHashMap<String, Map<String, String>> pastWeeksResponses = new LinkedHashMap<>();
    for (Map.Entry<String, List<ParticipantChartInfoBo>> weekResponses :
        surveyResponsesGroupedByWeek.entrySet()) {
      Map<String, String> activityScoreMap = new HashMap<>();
      for (ParticipantChartInfoBo participantChartInfo : weekResponses.getValue()) {
        activityScoreMap.put(
            participantChartInfo.getActivityId(), participantChartInfo.getQuestionResponse());
      }
      pastWeeksResponses.put(weekResponses.getKey(), activityScoreMap);
    }
    return pastWeeksResponses;
  }

  private StringBuffer generateCurrentWeekContent(Map<String, String> activityScoreMap) {
    logger.info("ParticipantSummaryReportServiceImpl generateCurrentWeekContent() - starts ");
    StringBuffer currentReport = new StringBuffer();
    try {
      TableConfigUtil tableConfigUtil = new TableConfigUtil(AppUtil.getTableJsonConfig());
      currentReport.append("<style>");
      currentReport.append(tableConfigUtil.getCSS());
      currentReport.append("</style>");
      currentReport.append("<p><b>You reported that this past week:</b></p>");
      currentReport.append(tableConfigUtil.makeHtmlTable(activityScoreMap));
      currentReport.append(
          "<p><b>About Your Report:</b> These Heroes Health reports<br>"
              + "are to help you monitor your symptoms and mental health over<br>"
              + "time. They should not be interpreted as a diagnosis. If you<br>"
              + "have concerns about your mental health, contact your health<br>"
              + "care provider and/or use the resources listed in the resources<br>"
              + "tab.</p>");
    } catch (Exception e) {
      logger.error("ParticipantSummaryReportServiceImpl generateCurrentWeekContent() - error ", e);
    }
    logger.info("ParticipantSummaryReportServiceImpl generateCurrentWeekContent() - Ends ");
    return currentReport;
  }

  private StringBuffer generatePastWeekContent(
      Map<String, Map<String, String>> pastWeeksResponses) {
    logger.info("ParticipantSummaryReportServiceImpl generatePastWeekContent() - starts ");
    StringBuffer pastReport = new StringBuffer();
    try {
      TableConfigUtil tableConfigUtil = new TableConfigUtil(AppUtil.getTableJsonConfig());
      pastReport.append("<style>");
      pastReport.append(tableConfigUtil.getCSS());
      pastReport.append("</style>");
      for (Map.Entry<String, Map<String, String>> weekResponse : pastWeeksResponses.entrySet()) {
        pastReport.append("<p><b>Week of " + weekResponse.getKey() + "</b></p>");
        pastReport.append(tableConfigUtil.makeHtmlTable(weekResponse.getValue()));
      }
      pastReport.append(
          "<p><b>About Your Report:</b> These Heroes Health reports<br>"
              + "are to help you monitor your symptoms and mental health over<br>"
              + "time. They should not be interpreted as a diagnosis. If you<br>"
              + "have concerns about your mental health, contact your health<br>"
              + "care provider and/or use the resources listed in the resources<br>"
              + "tab.</p>");
    } catch (Exception e) {
      logger.error("ParticipantSummaryReportServiceImpl generatePastWeekContent() - error ", e);
    }
    logger.info("ParticipantSummaryReportServiceImpl generatePastWeekContent() - Ends ");
    return pastReport;
  }

  @Override
  public void saveSummaryReports(String participantId) {
    logger.info("ParticipantSummaryReportServiceImpl saveSummaryReports() - Starts ");
    try {
      summaryReportUtil.saveSummaryReports(participantId, generateSummaryReports(participantId));
    } catch (Exception e) {
      logger.error("ParticipantSummaryReportServiceImpl saveSummaryReports() - Error ", e);
    }
    logger.info("ParticipantSummaryReportServiceImpl saveSummaryReports() - Ends ");
  }
}

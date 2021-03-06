package com.google.cloud.healthcare.fdamystudies.utils;

import com.google.cloud.healthcare.fdamystudies.bean.SummaryReportBean;
import com.google.cloud.healthcare.fdamystudies.config.ApplicationConfiguration;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SummaryReportUtil {

  private static final Logger logger = LoggerFactory.getLogger(SummaryReportUtil.class);

  @Autowired private RestTemplate restTemplate;

  @Autowired private ApplicationConfiguration appConfig;

  public void saveSummaryReports(String participantId, List<SummaryReportBean> summaryReports) {
    logger.info("SummaryReportUtil saveSummaryReports() - starts ");
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set(AppConstants.PARTICIPANT_ID_KEY, participantId);
      headers.set(AppConstants.CLIENT_ID_PARAM, appConfig.getRegServerClientId());
      headers.set(
          AppConstants.CLIENT_SECRET_PARAM,
          ResponseServerUtil.getHashedValue(appConfig.getRegServerClientSecret()));
      HttpEntity<?> request = new HttpEntity<>(summaryReports, headers);
      restTemplate.exchange(
          appConfig.getRegServerSaveSummaryReportsUrl(), HttpMethod.POST, request, String.class);
    } catch (Exception e) {
      logger.error("SummaryReportUtil saveSummaryReports() - error ", e);
    }
    logger.info("SummaryReportUtil saveSummaryReports() - ends ");
  }

  public static boolean isDateInCurrentWeek(Date date) {
    logger.info("SummaryReportUtil checkCurrentWeek() - starts ");
    boolean currentWeek = false;
    try {
      Calendar currentCalendar = Calendar.getInstance();
      int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
      int year = currentCalendar.get(Calendar.YEAR);
      Calendar targetCalendar = Calendar.getInstance();
      targetCalendar.setTime(date);
      int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
      int targetYear = targetCalendar.get(Calendar.YEAR);
      currentWeek = week == targetWeek && year == targetYear;
    } catch (Exception e) {
      logger.error("SummaryReportUtil checkCurrentWeek() - error ", e);
    }
    logger.info("SummaryReportUtil checkCurrentWeek() - ends ");

    return currentWeek;
  }

  public static String startOfDate(String date) throws ParseException {
    String WeekStartDate = "";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date inputDate = sdf.parse(date);
    Calendar cal = Calendar.getInstance();
    cal.setTime(inputDate);
    int getDayOfTheWeek = 0;
    int dayOfTheWeek = cal.get(Calendar.DAY_OF_WEEK);

    if (dayOfTheWeek == 1) {
      getDayOfTheWeek = 1;
      WeekStartDate = sdf.format(cal.getTime());
    } else {
      getDayOfTheWeek = dayOfTheWeek - 1;
      LocalDate minusDate =
          LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId()).toLocalDate();
      LocalDate returnWeekStartValue = minusDate.minusDays(getDayOfTheWeek);
      WeekStartDate = returnWeekStartValue.toString();
    }
    return WeekStartDate;
  }
}

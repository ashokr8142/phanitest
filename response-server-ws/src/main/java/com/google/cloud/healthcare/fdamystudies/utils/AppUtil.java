/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package com.google.cloud.healthcare.fdamystudies.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.healthcare.fdamystudies.bean.ActivityList;
import com.google.cloud.healthcare.fdamystudies.bean.ActivityListWrapper;
import com.google.cloud.healthcare.fdamystudies.bean.ErrorBean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ResourceUtils;

public class AppUtil {
  private static final Logger logger = LogManager.getLogger(AppUtil.class);

  public static ErrorBean dynamicResponse(
      int code, String userMessage, String type, String detailMessage) {
    ErrorBean error = null;
    try {
      error = new ErrorBean(code, userMessage, type, detailMessage);
    } catch (Exception e) {
      logger.error("ERROR: AppUtil - dynamicResponse() - error()", e);
    }
    return error;
  }

  public static String makeStudyCollectionName(String studyId) {
    if (!StringUtils.isBlank(studyId)) {
      return studyId + AppConstants.HYPHEN + AppConstants.RESPONSES;
    }
    return null;
  }

  public static String makeParticipantCollectionName(String studyId, String siteId) {
    if (!StringUtils.isBlank(studyId) && !StringUtils.isBlank(siteId)) {
      return studyId
          + AppConstants.HYPHEN
          + siteId
          + AppConstants.HYPHEN
          + AppConstants.PARTICIPANT_METADATA_KEY;
    }
    return null;
  }

  public static String makeActivitiesCollectionName(String studyId, String siteId) {
    if (!StringUtils.isBlank(studyId) && !StringUtils.isBlank(siteId)) {
      return studyId
          + AppConstants.HYPHEN
          + siteId
          + AppConstants.HYPHEN
          + AppConstants.ACTIVITIES_COLLECTION_NAME;
    }
    return null;
  }

  public static Map<String, Activity> getChartJsonConfig() throws IOException {
    BufferedReader br = null;
    Map<String, Activity> activityConfigMap = new HashMap<String, Activity>();

    File file = ResourceUtils.getFile("classpath:chart_config_params.json");
    try {
      br = new BufferedReader(new FileReader(file));
      ObjectMapper mapper = new ObjectMapper();
      ChartJsonConfig obj = mapper.readValue(br, ChartJsonConfig.class);
      if (obj != null) {
        List<Activity> actvityParamList = obj.getActivityList();
        if (!actvityParamList.isEmpty()) {
          for (Activity tempActivity : actvityParamList) {
            if (tempActivity != null) {
              activityConfigMap.put(tempActivity.getActivityId(), tempActivity);
            }
          }
        }
      }
    } catch (Exception ex) {
      logger.error("ERROR: AppUtil - getChartJsonConfig() - error()", ex.getMessage());
      return null;
    } finally {

      if (br != null) {
        br.close();
      }
      return activityConfigMap;
    }
  }

  public static TableJsonConfigMapping getTableJsonConfig() {
    BufferedReader br = null;
    TableJsonConfigMapping tableJsonConfigMapping = null;
    try {
      File file = ResourceUtils.getFile("classpath:table_config.json");
      br = new BufferedReader(new FileReader(file));
      ObjectMapper mapper = new ObjectMapper();
      TableJsonConfig jsonConfig = mapper.readValue(br, TableJsonConfig.class);
      if (jsonConfig != null) {
        tableJsonConfigMapping = new TableJsonConfigMapping();
        List<Questionnaire> questionnaireList = jsonConfig.getQuestionnaires();
        Map<String, String> colors = jsonConfig.getColors();
        String defaultColor = jsonConfig.getDefaultColor();
        Map<String, Questionnaire> questionnaireConfigMap =
            new LinkedHashMap<String, Questionnaire>();
        if (!questionnaireList.isEmpty()) {
          for (Questionnaire questionnaire : questionnaireList) {
            if (questionnaire != null) {
              questionnaireConfigMap.put(questionnaire.getCanonicalId(), questionnaire);
            }
          }
        }
        tableJsonConfigMapping.setQuestionnaireConfigMap(questionnaireConfigMap);
        tableJsonConfigMapping.setColors(colors);
        tableJsonConfigMapping.setDefaultColor(defaultColor);
      }
    } catch (Exception ex) {
      logger.error("ERROR: AppUtil - getTableConfig() - error()", ex);
      return null;
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          logger.error("ERROR: AppUtil - getTableConfig() - error()", e.getMessage());
        }
      }
    }
    return tableJsonConfigMapping;
  }

  public static Map<String, String> getquestionIdActivityIdConfig() throws IOException {
    BufferedReader br = null;

    Map<String, String> questionIdActivityIdConfigMap = new HashMap<>();
    File file = ResourceUtils.getFile("classpath:questionId_activityId_mapping.json");
    try {
      br = new BufferedReader(new FileReader(file));
      ObjectMapper mapper = new ObjectMapper();
      ActivityListWrapper activityList = mapper.readValue(br, ActivityListWrapper.class);
      if (activityList != null) {
        for (ActivityList activity : activityList.getActivityList()) {
          for (String questionId : activity.getQuestionIds()) {
            questionIdActivityIdConfigMap.put(questionId.toLowerCase(), activity.getActivityId());
          }
        }
      }
    } catch (Exception ex) {
      logger.error("ERROR: AppUtil - getquestionIdActivityIdConfig() - error()", ex.getMessage());
      return null;
    } finally {

      if (br != null) {
        br.close();
      }
    }
    return questionIdActivityIdConfigMap;
  }
}

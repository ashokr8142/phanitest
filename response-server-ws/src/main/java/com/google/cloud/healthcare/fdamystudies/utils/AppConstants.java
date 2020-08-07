/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package com.google.cloud.healthcare.fdamystudies.utils;

public class AppConstants {
  public static final String HYPHEN = "-";
  public static final String RESPONSES = "RESPONSES";
  public static final String EMPTY_STR = "";
  public static final String FAILURE = "FAILURE";
  public static final int RESPONSE_DATA_SAVE_RETRY_ATTEMPTS = 3;
  public static final String JSON_FILE_EXTENSION = ".json";
  public static final String FILE_SEPARATOR = "/";
  public static final String RESPONSE_DATA_SCHEMA_NAME_LEGACY = "MobileAppResponse";
  public static final String RESPONSE_DATA_QUERY_NAME_LEGACY = "firestore_response_query";
  public static final String TRUE_STR = "true";
  public static final String VALUE_KEY_STR = "value";
  public static final String PARTICIPANT_ID_KEY = "participantId";
  public static final String PARTICIPANT_ID_RESPONSE = "ParticipantId";
  public static final String CREATED_TS_KEY = "createdTimestamp";
  public static final String ISO_DATE_FORMAT_RESPONSE = "yyyy-MM-dd'T'HH:mm:ss:SSSZZZZZ";
  public static final String CREATED_RESPONSE = "Created";
  public static final String RESULT_TYPE_KEY = "resultType";
  public static final String QUESTION_ID_KEY = "key";
  // TODO: The score sum feature should be properly implemented. This dummy question
  // approach is a short-term workaround.
  public static final String DUMMY_SUM_QUESTION_KEY = "_SUM";
  public static final String FIELD_PATH_ACTIVITY_ID = "metadata.activityId";
  public static final String PARTICIPANT_TOKEN_IDENTIFIER_KEY = "tokenIdentifier";
  public static final String PARTICIPANT_IDENTIFIER_KEY = "participantIdentifier";
  public static final String GROUPED_FIELD_KEY = "grouped";
  public static final Object DATA_FIELD_KEY = "data";
  public static final String RESULTS_FIELD_KEY = "results";
  public static final String RAW_RESPONSE_FIELD_KEY = "rawResponseData";
  public static final Object PROPERTY_NAME_CLASS = "class";
  public static final String CLIENT_TOKEN_KEY = "clientToken";
  public static final String USER_ID_KEY = "userId";
  public static final String ACCESS_TOKEN_KEY = "accessToken";
  public static final String ORG_ID_HEADER = "orgId";
  public static final String APPLICATION_ID_HEADER_WCP = "applicationId";
  public static final String APPLICATION_ID_HEADER = "appId";
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String STUDY_ID_PARAM = "studyId";
  public static final String ACTIVITY_ID_KEY = "activityId";
  public static final String ACTIVITY_RUN_ID_KEY = "activityRunId";
  public static final String ACTIVITY_VERSION_PARAM = "activityVersion";
  public static final String ERROR_STR = "Error";
  public static final String OPTIONS_METHOD = "OPTIONS";
  public static final String CLIENT_ID_PARAM = "clientId";
  public static final String CLIENT_SECRET_PARAM = "secretKey";
  public static final String SITE_ID_KEY = "siteId";
  public static final String STUDY_VERSION_KEY = "studyVersion";
  public static final String SHARING_CONSENT_KEY = "sharingConsent";
  public static final String ACTIVITY_TYPE_KEY = "activityType";
  public static final String ACTIVITY_TYPE_TASK = "task";
  public static final String PARTICIPANT_METADATA_KEY = "Participants";
  public static final String ACTIVITIES_COLLECTION_NAME = "Activities";
  public static final String SUCCESS_MSG = "SUCCESS";
  public static final String COMPLETED = "Completed";
  public static final String WITHDRAWAL_STATUS_KEY = "withdrawalStatus";
  public static final int FS_BATCH_COMMIT_LIMIT = 500;

  public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
  public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
  public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
  public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
  public static final String HTTP_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
  public static final String CODE = "code";
  public static final String USER_MESSAGE = "userMessage";
  public static final String TYPE = "type";
  public static final String DETAIL_MESSAGE = "detailMessage";
  public static final String BASIC_PREFIX = "Basic ";
  public static final String SYSTEM_USER = "RESPONSE_SERVER";
  public static final String COMMA_STR = ",";
  public static final String SQUARE_BRACKET_OPEN = "[";
  public static final String SQUARE_BRACKET_CLOSE = "]";
  public static final String SEMI_COLON = ";";
  public static final String PSQIPUBLIC1_ACTIVITY_ID = "PSQIPublic1";
  public static final String PHQ9PUBLIC_ACTIVITY_ID = "PHQ9Public";
  public static final String GAD7PUBLIC1_ACTIVITY_ID = "GAD7Public1";
  public static final String PTSDPUBLIC_ACTIVITY_ID = "PTSDPublic";
  public static final String WSASPUBLIC_ACTIVITY_ID = "WSASPublic";
  public static final String CHART_HTML_STR_START =
      "<!DOCTYPE html>\n"
          + "<html>\n"
          + "<head>\n"
          + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n"
          + "   <script src=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.2/Chart.min.js\"></script>\n"
          + "  </head>\n"
          + "<body>\n"
          + "<canvas height=\"50\" id=\"PSQIPublic1\"></canvas>\n"
          + "<canvas height=\"51\" id=\"PHQ9Public\"></canvas>\n"
          + "<canvas height=\"51\" id=\"GAD7Public1\"></canvas>\n"
          + "<canvas height=\"51\" id=\"PTSDPublic\"></canvas>\n"
          + "<canvas height=\"51\" id=\"WSASPublic\"></canvas>\n"
          + "<script>";
  public static final String CHART_HTML_CHART_PREFIX_1 = "var {0}Labels = ";
  public static final String CHART_HTML_CHART_PREFIX_2 = "var {0}Data = ";
  public static final String CHART_HTML_CHART_DYNAMIC =
      "var ctx{0} = document.getElementById(''{0}'').getContext(''2d'');\n"
          + " const gradient{0} = ctx{0}.createLinearGradient(0, 0, 0, 750);\n"
          + "        gradient{0}.addColorStop(0, ''rgba(250,174,50,1)'');   \n"
          + "        gradient{0}.addColorStop(1, ''rgba(250,174,50,0)'');\n"
          + "        gradient{0}.addColorStop(1, ''rgba(255, 0, 0, 0.6)'');\n"
          + "\n"
          + "        var  gr{0}  = ctx{0}.createLinearGradient(0, 0, 0, 700);\n"
          + "  gr{0}.addColorStop(0,''rgba(250,174,50,1)''); \n"
          + "  gr{0}.addColorStop(0.8,''rgba(250,174,50,0)''); \n"
          + "  // gr.addColorStop(0.8,''rgb(0,255,0.8)'');\n"
          + "   //gr.addColorStop(0.5,''rgba(255, 0, 0, 0.4)'');\n"
          + "\n"
          + "   var gradientFill = ctx{0}.createLinearGradient(0, 0, 0, 400);\n"
          + "gradientFill.addColorStop(0, ''rgba(128, 182, 244, 0.6)'');\n"
          + "gradientFill.addColorStop(1, ''rgba(244, 144, 128, 0.6)'');\n"
          + "\n"
          + "var myChart{0} = new Chart(ctx{0}, '{'\n"
          + "    type: ''line'',\n"
          + "    data: '{'\n"
          + "        labels:{0}Labels,\n"
          + "        datasets: ['{'\n"
          + "            label:true,\n"
          + "            data:{0}Data,\n"
          + "            backgroundColor : gradientFill, "
          + "            borderColor : ''# '',\n"
          + "            borderWidth: 2,\n"
          + "            pointColor : ''#fff'',\n"
          + "            pointStrokeColor : ''#ff6c23'',\n"
          + "            pointHighlightFill: ''#fff'',\n"
          + "            pointHighlightStroke: ''#ff6c23'',\n"
          + "       '}']\n"
          + "    '}',\n";

  public static final String CHART_HTML_CHART_STATIC =
      "options: {\n"
          + "        scales: {\n"
          + "            yAxes: [{\n"
          + "              ticks: {\n"
          + "                display:false,\n"
          + "                stepSize: 1,\n"
          + "                min: 0,\n"
          + "                max: 7\n"
          + "              },\n"
          + "              drawBorder: false\n"
          + "            }],\n"
          + "            xAxes : [ {\n"
          + "               ticks: {\n"
          + "              //maxRotation: 90,\n"
          + "              //minRotation: 80,\n"
          + "            },\n"
          + "            gridLines : {\n"
          + "              display : false\n"
          + "            }\n"
          + "          }]\n"
          + "        },\n"
          + "    },\n"
          + "      \n"
          + "});";

  public static final String CHART_HTML_STR_END = "</script>\n" + "</body>\n" + "</html>";
}

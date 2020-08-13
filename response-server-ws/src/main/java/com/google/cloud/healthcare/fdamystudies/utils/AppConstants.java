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
  public static final String NO_CHART_DISPLAY_INFORMATION = "Unable to display chart information.";
  public static final String CHART_HTML_STR_START =
      "<!DOCTYPE html>\n"
          + "<head>\n"
          + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
          + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
          + "  <link href=\"https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap\" rel=\"stylesheet\">\n"
          + "  <script src=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.2/Chart.min.js\"></script>\n"
          + "\n"
          + "  <style>\n"
          + "    body{\n"
          + "      background: #E1E3E9;\n"
          + "      margin: 0;\n"
          + "      padding: 30px 0px;\n"
          + "      font-family: 'Roboto', sans-serif;\n"
          + "    }\n"
          + "    .canvas-container{\n"
          + "      background: #fff;\n"
          + "      padding: 15px;\n"
          + "    }\n"
          + "    .title{\n"
          + "      font-size:20px;\n"
          + "      margin-bottom:10px;\n"
          + "      padding: 0 15px;\n"
          + "    }\n"
          + "    .main-container{\n"
          + "      margin-bottom:20px;\n"
          + "    }\n"
          + "    .flex-container{\n"
          + "     /* display: flex;*/\n"
          + "      justify-content: space-between;\n"
          + "      text-align:center;\n"
          + "    }\n"
          + "    .months{\n"
          + "      color:green;\n"
          + "    }\n"
          + "    #left-arrow,\n"
          + "    #right-arrow{\n"
          + "        cursor:pointer;\n"
          + "    }\n"
          + "  </style>\n"
          + "\n"
          + "</head>\n"
          + "<body>";
  public static final String CHART_HTML_STR_CONTAINER_DYNAMIC =
      "<div class=\"main-container\">\n"
          + "    <div class=\"title\">{0}</div>\n"
          + "    <div class=\"canvas-container\">\n"
          + "      <div class=\"flex-container\">\n"
          + "        <!-- <div id=\"left-arrow\" onclick=\"alert('left arrow clicked')\"><img src=\"chevron-left.svg\"></div> -->\n"
          + "        <div class=\"months\">{1}</div>\n"
          + "        <!-- <div id=\"right-arrow\" onclick=\"alert('right arrow clicked')\"><img src=\"chevron-right.svg\"></div> -->\n"
          + "      </div>\n"
          + "      <canvas style=\"width:100%\" id=''{2}''></canvas>\n"
          + "      \n"
          + "    </div>";
  public static final String CHART_HTML_CHART_PREFIX_SCRIPT = "<script>";
  public static final String CHART_HTML_CHART_PREFIX_1 = "var {0}Labels = ";
  public static final String CHART_HTML_CHART_PREFIX_2 = "var {0}Data = ";
  public static final String CHART_HTML_CHART_DYNAMIC =
      "var ctx{0} = document.getElementById(''{0}'').getContext(''2d'');\n"
          + "   var gradientFill = ctx{0}.createLinearGradient(0, 0, 0, 150);\n"
          + "gradientFill.addColorStop(0.2, ''rgba(254, 111, 90, 0.6)'');\n"
          + "gradientFill.addColorStop(0.4, ''rgba(253, 187, 108, 0.6)'');\n"
          + "gradientFill.addColorStop(0.6, ''rgba(253, 241, 166, 0.6)'');\n"
          + "gradientFill.addColorStop(0.8, ''rgba(151, 237, 142, 0.8)'');\n"
          + "\n"
          + "var myChart{0} = new Chart(ctx{0}, '{'\n"
          + "    type: ''line'',\n"
          + "    data: '{'\n"
          + "        labels:{0}Labels,\n"
          + "        datasets: ['{'\n"
          + "            label:true,\n"
          + "            data:{0}Data,\n"
          + "            backgroundColor : gradientFill, "
          + "            borderWidth: 1,\n"
          + "            borderColor: \"#2f6942\",\n"
          + "            pointBackgroundColor: \"#2f6942\",\n"
          + "            pointRadius: 1.5,"
          + "       '}']\n"
          + "    '}',\n";

  public static final String CHART_HTML_CHART_STATIC_1 =
      "options: {\n"
          + "      responsive: false,\n"
          + "      legend: {\n"
          + "            display: false,\n"
          + "         },\n"
          + "        scales: {\n"
          + "            yAxes: [{\n"
          + "              colo:'#dce4eb',\n"
          + "              ticks: {\n"
          + "                stepSize: 1,\n"
          + "                min: 0,\n"
          + "                max: 4,\n"
          + "                autoSkip:true,\n"
          + "                fontSize: 12,\n"
          + "                fontStyle: \"bold\",\n"
          + "                callback: function(label, index, labels) {\n";
  public static final String CHART_HTML_CHART_AXIS_LABELS_1 =
      "switch (label) '{'" + " case 0:\n" + " return ''{0}'';\n" + "'}'";

  public static final String CHART_HTML_CHART_AXIS_LABELS_2 =
      "switch (label) '{'"
          + " case 0:\n"
          + " return ''{0}'';\n"
          + "                        case 1:\n"
          + "                            return ''{1}'';\n"
          + "'}'";
  public static final String CHART_HTML_CHART_AXIS_LABELS_3 =
      "switch (label) '{'"
          + " case 0:\n"
          + " return ''{0}'';\n"
          + "                        case 1:\n"
          + "                            return ''{1}'';\n"
          + "                        case 2:\n"
          + "                            return ''{2}'';\n"
          + "'}'";
  public static final String CHART_HTML_CHART_AXIS_LABELS_4 =
      "switch (label) '{'"
          + " case 0:\n"
          + " return ''{0}'';\n"
          + "                        case 1:\n"
          + "                            return ''{1}'';\n"
          + "                        case 2:\n"
          + "                            return ''{2}'';\n"
          + "                        case 3:\n"
          + "                            return ''{3}'';\n"
          + "'}'";
  public static final String CHART_HTML_CHART_AXIS_LABELS_5 =
      "switch (label) '{'"
          + " case 0:\n"
          + " return ''{0}'';\n"
          + "                        case 1:\n"
          + "                            return ''{1}'';\n"
          + "                        case 2:\n"
          + "                            return ''{2}'';\n"
          + "                        case 3:\n"
          + "                            return ''{3}'';\n"
          + "                        case 4:\n"
          + "                            return ''{4}'';\n"
          + "'}'";

  public static final String CHART_HTML_CHART_STATIC_2 =
      "\n"
          + "                }\n"
          + "              },\n"
          + "              drawBorder: false,\n"
          + "              scaleLabel: {\n"
          + "                    display: false,\n"
          + "                    labelString: 'Sleep Quality',\n"
          + "                    fontSize: 15,\n"
          + "                    fontStyle: \"bold\",\n"
          + "                }\n"
          + "            }],\n"
          + "            xAxes : [ {\n"
          + "              ticks: {\n"
          + "               fontSize: 12,\n"
          + "               fontStyle: \"bold\",\n"
          + "               maxRotation: 180\n"
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
  public static final String VERY_GOOD_LABEL = "Very good";
  public static final String FAIRLY_GOOD_LABEL = "Fairly good";
  public static final String FAIRLY_BAD_LABEL = "Fairly bad";
  public static final String VERY_BAD_LABEL = "Very bad";
  public static final int MAX_AXIS_LABEL_LIST_SIZE = 5;
  public static final String SPACE_STR = " ";
  public static final String NO_DATA_AVAILABLE = "There is no data available for this time period";
  public static final String CHART_ERROR_HTML =
      "<!DOCTYPE html>\n"
          + "<head>\n"
          + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
          + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
          + "    <link\n"
          + "        href=\"https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap\"\n"
          + "        rel=\"stylesheet\">\n"
          + "    \n"
          + "    <style>\n"
          + "        \n"
          + "        .title {\n"
          + "            font-size: 20px;\n"
          + "            margin-bottom: 10px;\n"
          + "            padding: 0 15px;\n"
          + "        }\n"
          + "\n"
          + "        .main-container {\n"
          + "            margin-bottom: 20px;\n"
          + "        }\n"
          + "\n"
          + "        .flex-container {\n"
          + "            /* display: flex;*/\n"
          + "            justify-content: space-between;\n"
          + "            text-align: center;\n"
          + "        }\n"
          + "    </style>\n"
          + "</head>\n"
          + "<body>\n"
          + "    <div class=\"main-container\">\n"
          + "    There was an error retrieving the chart information.\n"
          + "        </div>\n"
          + "</body>\n"
          + "</html>";
}

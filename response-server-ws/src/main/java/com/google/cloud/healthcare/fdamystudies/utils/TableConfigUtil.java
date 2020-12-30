package com.google.cloud.healthcare.fdamystudies.utils;

import com.google.cloud.healthcare.fdamystudies.bean.ActivityMessageBean;
import com.google.cloud.healthcare.fdamystudies.bean.TextAndColorBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class TableConfigUtil {

  private Map<String, Questionnaire> questionnaireMap = null;
  private Map<String, String> activityIdToCanonicalId = null;
  private Map<String, ActivityMessageBean> canonicalIdActivityMessageMap = null;
  private Map<String, String> colors = null;
  private String defaultColor;

  public TableConfigUtil(TableJsonConfigMapping tableJsonConfigUtil) {
    questionnaireMap = tableJsonConfigUtil.getQuestionnaireConfigMap();
    colors = tableJsonConfigUtil.getColors();
    defaultColor = tableJsonConfigUtil.getDefaultColor();
    activityIdToCanonicalId = new HashMap<String, String>();
    canonicalIdActivityMessageMap = getQuestionnaireInfoDetails();
  }

  public Map<String, ActivityMessageBean> getQuestionnaireInfoDetails() {
    Map<String, ActivityMessageBean> finalMap = new LinkedHashMap<String, ActivityMessageBean>();
    for (Map.Entry<String, Questionnaire> questionnaireEntry : questionnaireMap.entrySet()) {
      String canonicalId = questionnaireEntry.getKey();
      String title = questionnaireEntry.getValue().getTitle();
      List<String> activityIdList = questionnaireEntry.getValue().getActivityIds();
      for (String activityId : activityIdList) {
        if (!activityIdToCanonicalId.containsKey(activityId)) {
          activityIdToCanonicalId.put(activityId, canonicalId);
        }
      }

      List<Integer> ranges = new ArrayList<Integer>();
      List<TextAndColorBean> textAndColorList = new ArrayList<TextAndColorBean>();
      List<Bucket> bucketList = questionnaireEntry.getValue().getBuckets();
      for (Bucket bucket : bucketList) {
        Integer lowerBound = bucket.getLowerBound();
        ranges.add(lowerBound);
        TextAndColorBean textAndColor =
            new TextAndColorBean(bucket.getText(), bucket.getColorRef());
        textAndColorList.add(textAndColor);
      }
      ActivityMessageBean activityMessageBean =
          new ActivityMessageBean(title, ranges, textAndColorList);
      finalMap.put(canonicalId, activityMessageBean);
    }
    return finalMap;
  }

  public HashMap<String, String> convertToCanonicalId(Map<String, String> activityScoreMap) {
    HashMap<String, String> canonicalIdScoreMap = new HashMap<String, String>();
    for (Map.Entry<String, String> activityScoreEntry : activityScoreMap.entrySet()) {
      String activityId = activityScoreEntry.getKey();
      String canonicalId = activityIdToCanonicalId.get(activityId);
      if (null != canonicalId) {
        canonicalIdScoreMap.put(canonicalId, activityScoreEntry.getValue());
      }
    }
    return canonicalIdScoreMap;
  }

  public TextAndColorBean getMessageFromScore(String canonicalId, String score) {
    if (StringUtils.equalsAnyIgnoreCase(score, "None")) {
      return new TextAndColorBean("Not completed", defaultColor);
    } else if (!canonicalIdActivityMessageMap.containsKey(canonicalId)) {
      return new TextAndColorBean("Unknown survey", defaultColor);
    }

    int index =
        bisectRight(
            canonicalIdActivityMessageMap.get(canonicalId).getRanges().toArray(new Integer[0]),
            Math.round(Float.valueOf(score)));

    if (index > 0) {
      index = index - 1;
    }
    return canonicalIdActivityMessageMap.get(canonicalId).getTextColorsList().get(index);
  }

  public StringBuffer makeHtmlTable(Map<String, String> activityScoreMap) {
    StringBuffer htmlTable = new StringBuffer();
    htmlTable.append("<table>");
    HashMap<String, String> canonicalIdScoreMap = convertToCanonicalId(activityScoreMap);
    for (Map.Entry<String, ActivityMessageBean> canonicalIdActivityMessageEntry :
        canonicalIdActivityMessageMap.entrySet()) {
      String canonicalId = canonicalIdActivityMessageEntry.getKey();
      String score =
          canonicalIdScoreMap.get(canonicalId) != null
              ? canonicalIdScoreMap.get(canonicalId)
              : "None";
      TextAndColorBean textAndColorBean = getMessageFromScore(canonicalId, score);
      htmlTable.append(
          getHTMLTableRow(canonicalIdActivityMessageEntry.getValue().getTitle(), textAndColorBean));
    }
    htmlTable.append("</table>");
    return htmlTable;
  }

  public static int bisectRight(Integer[] A, int x) {
    return bisectRight(A, x, 0, A.length);
  }

  public static int bisectRight(Integer[] A, int x, int lo, int hi) {
    int N = A.length;
    if (N == 0) {
      return 0;
    }
    if (x < A[lo]) {
      return lo;
    }
    if (x > A[hi - 1]) {
      return hi;
    }
    for (; ; ) {
      if (lo + 1 == hi) {
        return lo + 1;
      }
      int mi = (hi + lo) / 2;
      if (x < A[mi]) {
        hi = mi;
      } else {
        lo = mi;
      }
    }
  }

  private StringBuffer getHTMLTableRow(String title, TextAndColorBean textAndColor) {
    String rawHTML = "<tr><td class='title'>$title</td><td class='value $color'>$text</td></tr>";
    rawHTML = rawHTML.replace("$title", title);
    rawHTML = rawHTML.replace("$color", textAndColor.getColor());
    rawHTML = rawHTML.replace("$text", textAndColor.getText());
    StringBuffer htmlRow = new StringBuffer(rawHTML);
    return htmlRow;
  }

  public StringBuffer getCSS() {
    StringBuffer commonCSS =
        new StringBuffer(
            "table, tr, td {border: 1px solid #999999;}table {border-collapse: collapse;}tr {height: 2.2rem;vertical-align: middle;}td.title {width: 67%;}td.value {width: 33%;text-align: center;}");
    StringBuffer cssBuffer = new StringBuffer();
    for (Map.Entry<String, String> color : colors.entrySet()) {
      String css = "td." + color.getKey() + " { background-color: " + color.getValue() + "; }";
      cssBuffer.append(css);
    }
    return commonCSS.append(cssBuffer);
  }
}

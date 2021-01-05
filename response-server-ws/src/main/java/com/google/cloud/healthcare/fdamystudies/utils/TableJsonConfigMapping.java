package com.google.cloud.healthcare.fdamystudies.utils;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TableJsonConfigMapping {

  private Map<String, Questionnaire> questionnaireConfigMap;
  private Map<String, String> colors;
  private String defaultColor;
}

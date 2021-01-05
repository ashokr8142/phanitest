package com.google.cloud.healthcare.fdamystudies.utils;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TableJsonConfig {
  private List<Questionnaire> questionnaires;
  private Map<String, String> colors;
  private String defaultColor;
}

package com.google.cloud.healthcare.fdamystudies.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Questionnaire {
  private String canonicalId;
  private List<String> activityIds;
  private String title;
  private List<Bucket> buckets = new ArrayList<>();
}

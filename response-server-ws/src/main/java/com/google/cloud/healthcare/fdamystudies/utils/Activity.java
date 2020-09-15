package com.google.cloud.healthcare.fdamystudies.utils;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Activity {
  private String activityId;
  private String title;
  private List<Buckets> buckets;
}

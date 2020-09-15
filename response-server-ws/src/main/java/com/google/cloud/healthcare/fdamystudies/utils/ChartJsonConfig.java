package com.google.cloud.healthcare.fdamystudies.utils;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ChartJsonConfig {
  private List<Activity> activityList;
}

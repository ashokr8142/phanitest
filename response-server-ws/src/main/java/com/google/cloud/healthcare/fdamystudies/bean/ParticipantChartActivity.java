package com.google.cloud.healthcare.fdamystudies.bean;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ParticipantChartActivity {
  private String activityId;
  private String activityIdTitle;
  private String activityIdIndex;
  private List<String> labels;
  private List<Integer> data;
  private List<String> axisLabels;
}

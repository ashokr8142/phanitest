package com.google.cloud.healthcare.fdamystudies.bean;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ParticipantChartBean {
  private String monthRange;
  private List<ParticipantChartActivity> chartActivityList;
}

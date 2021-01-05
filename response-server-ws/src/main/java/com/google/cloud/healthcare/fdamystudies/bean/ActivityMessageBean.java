package com.google.cloud.healthcare.fdamystudies.bean;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActivityMessageBean {
  private String title;
  private List<Integer> ranges;
  private List<TextAndColorBean> textColorsList;
}

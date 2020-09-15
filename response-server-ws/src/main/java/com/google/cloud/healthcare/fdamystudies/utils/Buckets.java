package com.google.cloud.healthcare.fdamystudies.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Buckets {
  private int lowerBound;
  private int upperBound;
  private int normalizedValue;
  private String text;
  private String colorRef;
}

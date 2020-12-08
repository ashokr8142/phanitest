package com.google.cloud.healthcare.fdamystudies.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bucket {
  private int lowerBound;
  private String text;
  private String colorRef;
}

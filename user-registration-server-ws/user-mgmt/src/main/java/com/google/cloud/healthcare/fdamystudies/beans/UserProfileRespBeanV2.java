package com.google.cloud.healthcare.fdamystudies.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRespBeanV2 {
  private String message;
  private ProfileRespBean profile = new ProfileRespBean();
  private SettingsRespBean settings = new SettingsRespBean();
  private InstitutionInfoBean institutionInfo;
}

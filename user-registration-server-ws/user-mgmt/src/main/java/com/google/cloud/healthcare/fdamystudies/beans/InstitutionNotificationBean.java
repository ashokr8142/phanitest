package com.google.cloud.healthcare.fdamystudies.beans;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InstitutionNotificationBean {
  private String notificationText;
  private String notificationType;
  private String notificationSubType;
  private String createdTime;
}

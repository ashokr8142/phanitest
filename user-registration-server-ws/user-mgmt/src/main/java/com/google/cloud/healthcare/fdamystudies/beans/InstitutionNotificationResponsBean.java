package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InstitutionNotificationResponsBean {
  private List<InstitutionNotificationBean> institutionNotifications;
}

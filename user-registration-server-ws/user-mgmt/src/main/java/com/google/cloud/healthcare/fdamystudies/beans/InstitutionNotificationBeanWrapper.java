package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InstitutionNotificationBeanWrapper {
  private List<InstitutionNotificationBean> institutionNotificationBean;
  ErrorBean errorBean;
}

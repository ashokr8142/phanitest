package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.InstitutionNotificationBeanWrapper;
import com.google.cloud.healthcare.fdamystudies.beans.NotificationBean;
import java.util.List;

public interface InstitutionNotificationService {
  public void saveNotifications(
      List<Integer> userIdsWithInstitution, NotificationBean notificationBean);

  public InstitutionNotificationBeanWrapper getInstitutionNotification(String userId, String appId);
}

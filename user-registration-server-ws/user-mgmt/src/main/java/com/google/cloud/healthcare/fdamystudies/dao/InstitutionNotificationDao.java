package com.google.cloud.healthcare.fdamystudies.dao;

import com.google.cloud.healthcare.fdamystudies.beans.NotificationBean;
import java.util.List;

public interface InstitutionNotificationDao {
  public void saveNotifications(
      List<Integer> userIdsWithInstitution, NotificationBean notificationBean);
}

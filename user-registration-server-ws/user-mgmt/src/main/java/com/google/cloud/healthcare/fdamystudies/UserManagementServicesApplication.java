/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies;

import com.google.cloud.healthcare.fdamystudies.beans.NotificationBean;
import com.google.cloud.healthcare.fdamystudies.beans.NotificationForm;
import com.google.cloud.healthcare.fdamystudies.model.StateInstitutionMappingBO;
import com.google.cloud.healthcare.fdamystudies.repository.StateInstitutionMappingRepository;
import com.google.cloud.healthcare.fdamystudies.service.StudiesServices;
import com.google.cloud.healthcare.fdamystudies.service.UserManagementProfileService;
import com.google.cloud.healthcare.fdamystudies.util.AppConstants;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class UserManagementServicesApplication {

  @Resource private StateInstitutionMappingRepository stateInstitutionMappingRepository;

  @Autowired private UserManagementProfileService userManagementProfileService;

  @Autowired private StudiesServices studiesServices;

  public static void main(String[] args) {

    SpringApplication.run(UserManagementServicesApplication.class, args);
  }

  @Scheduled(cron = "0 0/10 * * * ?")
  public void sendPushNotification() {
    try {
      // fetch newly added Institutions
      boolean isNewInstitutionUpdated = false;
      List<StateInstitutionMappingBO> newInstitutionList =
          stateInstitutionMappingRepository.findByNewlyAdded(true);
      if (newInstitutionList != null & !newInstitutionList.isEmpty()) {
        isNewInstitutionUpdated =
            userManagementProfileService.updateNewlyAddedInstitutes(newInstitutionList);
      }
      // send push notification if institutions added
      if (isNewInstitutionUpdated) {
        NotificationForm notificationForm = new NotificationForm();
        List<NotificationBean> notifications = new ArrayList<NotificationBean>();
        NotificationBean notificationBean = new NotificationBean();
        notificationBean.setAppId(AppConstants.APPID);
        notificationBean.setNotificationSubType(AppConstants.NOTIFICATION_SUBTYPE);
        notificationBean.setNotificationText(AppConstants.NOTIFICATIONTEXT_NEW_INSTITUTION);
        notificationBean.setNotificationTitle("");
        notificationBean.setNotificationType(AppConstants.INSTITUTION_LEVEL);
        notifications.add(notificationBean);
        notificationForm.setNotifications(notifications);
        studiesServices.SendNotificationAction(notificationForm);
      }

      // fetch institutions to be removed
      boolean isInstitutionRemoved = false;
      List<StateInstitutionMappingBO> institutionToRemoveList =
          stateInstitutionMappingRepository.findByToRemove(true);
      if (institutionToRemoveList != null && !institutionToRemoveList.isEmpty()) {
        isInstitutionRemoved =
            userManagementProfileService.removeInstitutions(institutionToRemoveList);
      }
      // send push notification if institutions removed
      if (isInstitutionRemoved) {
        NotificationForm notificationForm = new NotificationForm();
        List<NotificationBean> notifications = new ArrayList<NotificationBean>();
        NotificationBean notificationBean = new NotificationBean();
        notificationBean.setAppId(AppConstants.APPID);
        notificationBean.setNotificationSubType(AppConstants.NOTIFICATION_SUBTYPE);
        notificationBean.setNotificationText(AppConstants.NOTIFICATIONTEXT_INSTITUTION_REMOVED);
        notificationBean.setNotificationTitle("");
        notificationBean.setNotificationType(AppConstants.INSTITUTION_LEVEL);
        notifications.add(notificationBean);
        notificationForm.setNotifications(notifications);
        studiesServices.SendNotificationAction(notificationForm);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

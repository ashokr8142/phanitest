package com.google.cloud.healthcare.fdamystudies.scheduler;

import com.google.cloud.healthcare.fdamystudies.beans.NotificationBean;
import com.google.cloud.healthcare.fdamystudies.beans.NotificationForm;
import com.google.cloud.healthcare.fdamystudies.model.StateInstitutionMappingBO;
import com.google.cloud.healthcare.fdamystudies.repository.StateInstitutionMappingRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserDetailsBORepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserInstitutionRepository;
import com.google.cloud.healthcare.fdamystudies.service.InstitutionNotificationService;
import com.google.cloud.healthcare.fdamystudies.service.StudiesServices;
import com.google.cloud.healthcare.fdamystudies.service.UserManagementProfileService;
import com.google.cloud.healthcare.fdamystudies.util.AppConstants;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InstitutionSchedulerService {

  @Resource private StateInstitutionMappingRepository stateInstitutionMappingRepository;

  @Autowired private UserManagementProfileService userManagementProfileService;

  @Autowired private StudiesServices studiesServices;

  @Autowired UserInstitutionRepository userInstitutionRepository;

  @Autowired UserDetailsBORepository userDetailsBORepository;

  @Autowired InstitutionNotificationService institutionNotificationService;

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
        for (StateInstitutionMappingBO institution : newInstitutionList) {
          NotificationBean notificationBean = new NotificationBean();
          notificationBean.setAppId(AppConstants.APPID);
          notificationBean.setNotificationSubType(AppConstants.NOTIFICATION_SUBTYPE);
          String notificationText =
              AppConstants.NOTIFICATIONTEXT_NEW_INSTITUTION.replace(
                  "$institute", institution.getInstitutionId());
          notificationText = notificationText.replace("$state", institution.getState());
          notificationBean.setNotificationText(notificationText);
          notificationBean.setNotificationTitle("");
          notificationBean.setNotificationType(AppConstants.INSTITUTION_LEVEL);
          studiesServices.sendInstitutionNotification(notificationBean, null);
        }
      }

      // fetch institutions to be removed
      boolean isInstitutionRemoved = false;
      List<StateInstitutionMappingBO> institutionToRemoveList =
          stateInstitutionMappingRepository.findByToRemove(true);
      List<Integer> userIdsWithInstitutionAffiliation = null;
      if (institutionToRemoveList != null && !institutionToRemoveList.isEmpty()) {
        userIdsWithInstitutionAffiliation =
            userManagementProfileService.getUserIdsOfInstitutionsToBeRemoved(
                institutionToRemoveList);
        isInstitutionRemoved =
            userManagementProfileService.removeInstitutions(institutionToRemoveList);
      }
      // send push notification if institutions removed
      if (isInstitutionRemoved && !userIdsWithInstitutionAffiliation.isEmpty()) {
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
        studiesServices.sendInstitutionNotification(
            notificationBean, userIdsWithInstitutionAffiliation);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

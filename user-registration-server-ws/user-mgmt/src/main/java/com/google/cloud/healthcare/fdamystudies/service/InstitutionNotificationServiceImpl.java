package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.ErrorBean;
import com.google.cloud.healthcare.fdamystudies.beans.InstitutionNotificationBean;
import com.google.cloud.healthcare.fdamystudies.beans.InstitutionNotificationBeanWrapper;
import com.google.cloud.healthcare.fdamystudies.beans.NotificationBean;
import com.google.cloud.healthcare.fdamystudies.dao.InstitutionNotificationDao;
import com.google.cloud.healthcare.fdamystudies.model.InstitutionNotificationBO;
import com.google.cloud.healthcare.fdamystudies.model.UserDetailsBO;
import com.google.cloud.healthcare.fdamystudies.repository.InstitutionNotificationRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserDetailsBORepository;
import com.google.cloud.healthcare.fdamystudies.util.AppConstants;
import com.google.cloud.healthcare.fdamystudies.util.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InstitutionNotificationServiceImpl implements InstitutionNotificationService {

  private static final Logger logger =
      LoggerFactory.getLogger(InstitutionNotificationServiceImpl.class);

  @Autowired InstitutionNotificationDao institutionNotificationDao;

  @Autowired InstitutionNotificationRepository InstitutionNotificationRepository;

  @Autowired UserDetailsBORepository userDetailsBORepository;

  @Override
  public void saveNotifications(
      List<Integer> userIdsWithInstitution, NotificationBean notificationBean) {
    logger.info("InstitutionNotificationServiceImpl saveNotifications() - Starts ");
    institutionNotificationDao.saveNotifications(userIdsWithInstitution, notificationBean);
    logger.info("InstitutionNotificationServiceImpl saveNotifications() - Ends ");
  }

  @Override
  public InstitutionNotificationBeanWrapper getInstitutionNotification(
      String userId, String appId) {
    logger.info("InstitutionNotificationServiceImpl getInstitutionNotification() - Starts ");
    InstitutionNotificationBeanWrapper institutionNotificationBeanWrapper =
        new InstitutionNotificationBeanWrapper();
    ErrorBean errorBean = null;
    List<InstitutionNotificationBean> institutionNotificationBeanList = new ArrayList<>();
    try {
      UserDetailsBO userDetails = userDetailsBORepository.findByUserId(userId);
      if (userDetails != null) {
        Pageable topTwenty = PageRequest.of(0, 20);
        List<InstitutionNotificationBO> notificationList =
            InstitutionNotificationRepository.findByAppIdAndUserId(
                appId, userDetails.getUserDetailsId(), topTwenty);
        if (!notificationList.isEmpty()) {
          for (InstitutionNotificationBO notification : notificationList) {
            InstitutionNotificationBean institutionNotificationBean =
                new InstitutionNotificationBean();
            institutionNotificationBean.setNotificationText(notification.getNotificationText());
            institutionNotificationBean.setNotificationType(notification.getNotificationType());
            institutionNotificationBean.setNotificationSubType(
                notification.getNotificationSubType());
            institutionNotificationBean.setCreatedTime(
                notification.getCreatedTime() + AppConstants.UTC_TIMEZONE);
            institutionNotificationBeanList.add(institutionNotificationBean);
          }
          errorBean = new ErrorBean(ErrorCode.EC_200.code(), ErrorCode.EC_200.errorMessage());
        } else {
          errorBean = new ErrorBean(ErrorCode.EC_720.code(), ErrorCode.EC_720.errorMessage());
        }
        institutionNotificationBeanWrapper.setInstitutionNotificationBean(
            institutionNotificationBeanList);
      } else {
        errorBean = new ErrorBean(ErrorCode.EC_61.code(), ErrorCode.EC_61.errorMessage());
      }
      institutionNotificationBeanWrapper.setErrorBean(errorBean);
    } catch (Exception e) {
      logger.error("InstitutionNotificationServiceImpl getInstitutionNotification() - error ", e);
      errorBean = new ErrorBean(ErrorCode.EC_500.code(), ErrorCode.EC_500.errorMessage());
      institutionNotificationBeanWrapper.setErrorBean(errorBean);
    }
    logger.info("InstitutionNotificationServiceImpl getInstitutionNotification() - Ends ");
    return institutionNotificationBeanWrapper;
  }
}

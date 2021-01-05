package com.google.cloud.healthcare.fdamystudies.controller;

import com.google.cloud.healthcare.fdamystudies.beans.ErrorBean;
import com.google.cloud.healthcare.fdamystudies.beans.InstitutionNotificationBeanWrapper;
import com.google.cloud.healthcare.fdamystudies.beans.InstitutionNotificationResponsBean;
import com.google.cloud.healthcare.fdamystudies.service.InstitutionNotificationService;
import com.google.cloud.healthcare.fdamystudies.util.ErrorCode;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstitutionNotificationController {

  private static final Logger logger =
      LoggerFactory.getLogger(InstitutionNotificationController.class);

  @Autowired InstitutionNotificationService institutionNotificationService;

  @GetMapping(value = "/institutionNotification", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getInstitutionNotification(
      @RequestHeader String userId,
      @RequestHeader String appId,
      @Context HttpServletResponse response) {
    logger.info("InstitutionNotificationController getInstitutionNotification() - starts ");
    if (StringUtils.isBlank(userId) || StringUtils.isBlank(appId)) {
      ErrorBean errorBean = new ErrorBean(ErrorCode.EC_711.code(), ErrorCode.EC_711.errorMessage());
      logger.info("InstitutionNotificationController getInstitutionNotification() - Ends ");
      return new ResponseEntity<>(errorBean, HttpStatus.BAD_REQUEST);
    }
    InstitutionNotificationResponsBean notificationResponsBean =
        new InstitutionNotificationResponsBean();
    try {
      InstitutionNotificationBeanWrapper institutionNotificationBeanWrapper =
          institutionNotificationService.getInstitutionNotification(userId, appId);
      if (institutionNotificationBeanWrapper.getErrorBean().getCode() == ErrorCode.EC_720.code()
          || institutionNotificationBeanWrapper.getErrorBean().getCode()
              == ErrorCode.EC_61.code()) {
        logger.info("InstitutionNotificationController getInstitutionNotification() - Ends ");
        return new ResponseEntity<>(
            institutionNotificationBeanWrapper.getErrorBean(), HttpStatus.OK);
      } else if (institutionNotificationBeanWrapper.getErrorBean().getCode()
          == ErrorCode.EC_500.code()) {
        logger.error("InstitutionNotificationController getInstitutionNotification() - error ");
        return new ResponseEntity<>(
            institutionNotificationBeanWrapper.getErrorBean(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      notificationResponsBean.setInstitutionNotifications(
          institutionNotificationBeanWrapper.getInstitutionNotificationBean());
    } catch (Exception e) {
      logger.error("InstitutionNotificationController getInstitutionNotification() - error ", e);
      ErrorBean errorBean = new ErrorBean(ErrorCode.EC_500.code(), ErrorCode.EC_500.errorMessage());
      return new ResponseEntity<>(errorBean, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    logger.info("InstitutionNotificationController getInstitutionNotification() - Ends ");
    return new ResponseEntity<>(notificationResponsBean, HttpStatus.OK);
  }
}

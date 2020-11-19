package com.google.cloud.healthcare.fdamystudies.dao;

import com.google.cloud.healthcare.fdamystudies.beans.NotificationBean;
import com.google.cloud.healthcare.fdamystudies.model.InstitutionNotificationBO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class InstitutionNotificationDaoImpl implements InstitutionNotificationDao {

  private static final Logger logger =
      LoggerFactory.getLogger(InstitutionNotificationDaoImpl.class);

  @Autowired private EntityManagerFactory entityManagerFactory;

  @Override
  public void saveNotifications(
      List<Integer> userIdsWithInstitution, NotificationBean notificationBean) {
    logger.info("InstitutionNotificationDaoImpl saveNotifications() - Starts ");
    Transaction transaction = null;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      transaction = session.beginTransaction();
      DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
      for (Integer userId : userIdsWithInstitution) {
        InstitutionNotificationBO institutionNotificationBO = new InstitutionNotificationBO();
        institutionNotificationBO.setAppId(notificationBean.getAppId());
        institutionNotificationBO.setNotificationText(notificationBean.getNotificationText());
        institutionNotificationBO.setNotificationType(notificationBean.getNotificationType());
        institutionNotificationBO.setNotificationSubType(notificationBean.getNotificationSubType());
        institutionNotificationBO.setUserId(userId);
        institutionNotificationBO.setCreatedTime(pattern.format(LocalDateTime.now()));
        session.save(institutionNotificationBO);
      }
      transaction.commit();
    } catch (Exception e) {
      logger.error("InstitutionNotificationDaoImpl saveNotifications() - error ", e);
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error("InstitutionNotificationDaoImpl saveNotifications() - error rollback", e1);
        }
      }
    }
    logger.info("InstitutionNotificationDaoImpl saveNotifications() - Ends ");
  }
}

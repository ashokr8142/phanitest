package com.google.cloud.healthcare.fdamystudies.dao;

import com.google.cloud.healthcare.fdamystudies.beans.ErrorBean;
import com.google.cloud.healthcare.fdamystudies.beans.SummaryReportBean;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudiesBO;
import com.google.cloud.healthcare.fdamystudies.model.PersonalizedUserReportBO;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudiesRepository;
import com.google.cloud.healthcare.fdamystudies.util.ErrorCode;
import java.sql.Timestamp;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PersonalizedUserReportDaoImpl implements PersonalizedUserReportDao {

  private static final Logger logger = LoggerFactory.getLogger(PersonalizedUserReportDaoImpl.class);

  @Autowired private EntityManagerFactory entityManagerFactory;
  @Resource private ParticipantStudiesRepository participantStudiesRepository;

  @Override
  public ErrorBean savePersonalizedReports(
      String participantId, List<SummaryReportBean> summaryReports) {
    Transaction transaction = null;
    ErrorBean errorBean = null;
    logger.info("PersonalizedUserReportDaoImpl - savePersonalizedReports() - starts");
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      transaction = session.beginTransaction();
      ParticipantStudiesBO participantsBo =
          participantStudiesRepository.findByParticipantId(participantId);
      Query query =
          session.createQuery(
              "DELETE FROM PersonalizedUserReportBO PBO WHERE PBO.userDetails=:userDetails");
      query.setParameter("userDetails", participantsBo.getUserDetails());
      query.executeUpdate();
      for (SummaryReportBean report : summaryReports) {
        PersonalizedUserReportBO personalizedUserReport = new PersonalizedUserReportBO();
        personalizedUserReport.setReportTitle(report.getTitle());
        personalizedUserReport.setReportContent(report.getContent());
        personalizedUserReport.setCreationTime(new Timestamp(System.currentTimeMillis()));
        personalizedUserReport.setStudyInfo(participantsBo.getStudyInfo());
        personalizedUserReport.setUserDetails(participantsBo.getUserDetails());
        session.save(personalizedUserReport);
      }
      errorBean = new ErrorBean(ErrorCode.EC_200.code(), ErrorCode.EC_200.errorMessage());
      transaction.commit();
    } catch (Exception e) {
      logger.error("PersonalizedUserReportDaoImpl - savePersonalizedReports() - error ", e);
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error(
              "PersonalizedUserReportDaoImpl - savePersonalizedReports() - error rollback", e1);
        }
      }
      errorBean = new ErrorBean(ErrorCode.EC_500.code(), ErrorCode.EC_500.errorMessage());
    }
    logger.info("PersonalizedUserReportDaoImpl - savePersonalizedReports() - end");
    return errorBean;
  }
}

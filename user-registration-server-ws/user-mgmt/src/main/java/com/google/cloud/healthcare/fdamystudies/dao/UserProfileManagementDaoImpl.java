/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.dao;

import com.google.cloud.healthcare.fdamystudies.beans.ErrorBean;
import com.google.cloud.healthcare.fdamystudies.beans.InstitutionInfoBean;
import com.google.cloud.healthcare.fdamystudies.config.ApplicationPropertyConfiguration;
import com.google.cloud.healthcare.fdamystudies.model.AppInfoDetailsBO;
import com.google.cloud.healthcare.fdamystudies.model.AuthInfoBO;
import com.google.cloud.healthcare.fdamystudies.model.LoginAttemptsBO;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudiesBO;
import com.google.cloud.healthcare.fdamystudies.model.StateInstitutionMappingBO;
import com.google.cloud.healthcare.fdamystudies.model.StudyInfoBO;
import com.google.cloud.healthcare.fdamystudies.model.UserAppDetailsBO;
import com.google.cloud.healthcare.fdamystudies.model.UserDetailsBO;
import com.google.cloud.healthcare.fdamystudies.model.UserInstitution;
import com.google.cloud.healthcare.fdamystudies.repository.UserInstitutionRepository;
import com.google.cloud.healthcare.fdamystudies.util.AppConstants;
import com.google.cloud.healthcare.fdamystudies.util.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserProfileManagementDaoImpl implements UserProfileManagementDao {

  private static final Logger logger = LoggerFactory.getLogger(UserProfileManagementDaoImpl.class);

  @Autowired private EntityManagerFactory entityManagerFactory;

  @Autowired ApplicationPropertyConfiguration appConfig;

  @Autowired CommonDao commonDao;

  @Autowired private UserInstitutionRepository userInstitutionRepository;

  @Override
  public UserDetailsBO getParticipantInfoDetails(String userId) {

    logger.info("UserProfileManagementDaoImpl getParticipantInfoDetails() - Starts ");
    CriteriaBuilder criteriaBuilder = null;
    CriteriaQuery<UserDetailsBO> criteriaQuery = null;
    Root<UserDetailsBO> userDetailsBoRoot = null;
    Predicate[] predicates = new Predicate[1];
    List<UserDetailsBO> userDetailsBoList = null;
    UserDetailsBO userDetailsBO = null;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      criteriaBuilder = session.getCriteriaBuilder();
      criteriaQuery = criteriaBuilder.createQuery(UserDetailsBO.class);
      userDetailsBoRoot = criteriaQuery.from(UserDetailsBO.class);
      predicates[0] = criteriaBuilder.equal(userDetailsBoRoot.get(AppConstants.KEY_USERID), userId);
      criteriaQuery.select(userDetailsBoRoot).where(predicates);
      userDetailsBoList = session.createQuery(criteriaQuery).getResultList();
      if (!userDetailsBoList.isEmpty()) {
        userDetailsBO = userDetailsBoList.get(0);
      }
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl getParticipantInfoDetails() - error ", e);
    }
    logger.info("UserProfileManagementDaoImpl getParticipantInfoDetails() - Ends ");
    return userDetailsBO;
  }

  @Override
  public AuthInfoBO getAuthInfo(Integer userDetailsId) {
    logger.info("UserProfileManagementDaoImpl getAuthInfo() - Starts ");
    CriteriaBuilder criteriaBuilder = null;
    CriteriaQuery<AuthInfoBO> criteriaQuery = null;
    Root<AuthInfoBO> authInfoBoRoot = null;
    Predicate[] predicates = new Predicate[1];
    List<AuthInfoBO> authInfoBoList = null;
    AuthInfoBO authInfo = null;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      criteriaBuilder = session.getCriteriaBuilder();
      criteriaQuery = criteriaBuilder.createQuery(AuthInfoBO.class);
      authInfoBoRoot = criteriaQuery.from(AuthInfoBO.class);
      predicates[0] =
          criteriaBuilder.equal(authInfoBoRoot.get(AppConstants.KEY_USERID), userDetailsId);
      criteriaQuery.select(authInfoBoRoot).where(predicates);
      authInfoBoList = session.createQuery(criteriaQuery).getResultList();
      if (!authInfoBoList.isEmpty()) {
        authInfo = authInfoBoList.get(0);
      }
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl getAuthInfo() - error ", e);
    }
    logger.info("UserProfileManagementDaoImpl getAuthInfo() - Ends ");
    return authInfo;
  }

  @Override
  public ErrorBean updateUserProfile(
      String userId,
      UserDetailsBO userDetail,
      AuthInfoBO authInfo,
      InstitutionInfoBean institutionInfoBean) {
    logger.info("UserProfileManagementDaoImpl updateUserProfile() - Starts ");
    Transaction transaction = null;
    ErrorBean errorBean = null;
    Boolean isUpdatedAuthInfo = false;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      transaction = session.beginTransaction();

      if (null != userDetail) {
        session.saveOrUpdate(userDetail);
        errorBean = new ErrorBean(ErrorCode.EC_200.code(), ErrorCode.EC_200.errorMessage());
        if (null != authInfo) {
          session.saveOrUpdate(authInfo);
          isUpdatedAuthInfo = true;
        }

        Optional<UserInstitution> maybeUserInstitution =
            userInstitutionRepository.findByUserUserId(userId);
        if (institutionInfoBean != null) {
          if (maybeUserInstitution.isPresent()) {
            UserInstitution userInstitution = maybeUserInstitution.get();
            userInstitution.setInstitutionId(institutionInfoBean.getInstitutionId());
            userInstitution.setState(institutionInfoBean.getStateId());
            userInstitution.setStudyId(institutionInfoBean.getStudyId());
            session.update(userInstitution);
          } else {
            UserInstitution userInstitution = new UserInstitution();
            userInstitution.setUser(userDetail);
            userInstitution.setInstitutionId(institutionInfoBean.getInstitutionId());
            userInstitution.setState(institutionInfoBean.getStateId());
            userInstitution.setStudyId(institutionInfoBean.getStudyId());
            session.save(userInstitution);
          }
        } else {
          if (maybeUserInstitution.isPresent()) {
            Query query =
                session.createQuery(
                    "DELETE FROM UserInstitution UI WHERE UI.userInstitutionId=:userInstitutionId");
            query.setParameter(
                "userInstitutionId", maybeUserInstitution.get().getUserInstitutionId());
            query.executeUpdate();
          }
        }

      } else {
        errorBean = new ErrorBean(ErrorCode.EC_61.code(), ErrorCode.EC_61.errorMessage());
      }
      transaction.commit();
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl updateUserProfile() - error ", e);
      errorBean = new ErrorBean(ErrorCode.EC_34.code(), ErrorCode.EC_34.errorMessage());
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error("UserProfileManagementDaoImpl - updateUserProfile() - error rollback", e1);
        }
      }
    }
    logger.info("UserProfileManagementDaoImpl updateUserProfile() - Starts ");
    return errorBean;
  }

  @Override
  public UserDetailsBO getParticipantDetailsByEmail(
      String email, Integer appInfoId, Integer orgInfoId) {
    logger.info("UserProfileManagementDaoImpl getParticipantDetailsByEmail() - Starts ");
    UserDetailsBO userDetailsBO = null;
    CriteriaBuilder criteriaBuilder = null;
    CriteriaQuery<UserDetailsBO> criteriaQuery = null;
    Root<UserDetailsBO> userDetailsBoRoot = null;
    List<Predicate> userDetailsPredicates = new ArrayList<>();
    List<UserDetailsBO> userDetailsBoList = null;

    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      criteriaBuilder = session.getCriteriaBuilder();
      criteriaQuery = criteriaBuilder.createQuery(UserDetailsBO.class);
      userDetailsBoRoot = criteriaQuery.from(UserDetailsBO.class);

      userDetailsPredicates.add(
          criteriaBuilder.equal(userDetailsBoRoot.get(AppConstants.EMAIL), email));
      userDetailsPredicates.add(
          criteriaBuilder.equal(userDetailsBoRoot.get("appInfoId"), appInfoId));
      userDetailsPredicates.add(
          criteriaBuilder.notEqual(userDetailsBoRoot.get("emailCode"), "Null"));
      criteriaQuery
          .select(userDetailsBoRoot)
          .where(userDetailsPredicates.toArray(new Predicate[userDetailsPredicates.size()]));
      userDetailsBoList = session.createQuery(criteriaQuery).getResultList();
      if (!userDetailsBoList.isEmpty()) {
        userDetailsBO = userDetailsBoList.get(0);
      }
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl getParticipantInfoDetails() - error ", e);
    }
    logger.info("UserProfileManagementDaoImpl getParticipantDetailsByEmail() - Ends ");
    return userDetailsBO;
  }

  @Override
  public LoginAttemptsBO getLoginAttempts(String email) {
    logger.info("UserProfileManagementDaoImpl getParticipantDetailsByEmail() - Starts ");

    LoginAttemptsBO loginAttempt = null;
    CriteriaBuilder criteriaBuilder = null;
    CriteriaQuery<LoginAttemptsBO> criteriaQuery = null;
    Root<LoginAttemptsBO> loginAttemptRoot = null;
    Predicate[] predicates = new Predicate[1];
    List<LoginAttemptsBO> loginAttemptList = null;

    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      criteriaBuilder = session.getCriteriaBuilder();
      criteriaQuery = criteriaBuilder.createQuery(LoginAttemptsBO.class);
      loginAttemptRoot = criteriaQuery.from(LoginAttemptsBO.class);
      predicates[0] = criteriaBuilder.equal(loginAttemptRoot.get(AppConstants.EMAIL), email);
      criteriaQuery.select(loginAttemptRoot).where(predicates);
      loginAttemptList = session.createQuery(criteriaQuery).getResultList();
      if (!loginAttemptList.isEmpty()) {
        loginAttempt = loginAttemptList.get(0);
      }
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl getParticipantInfoDetails() - error ", e);
    }
    logger.info("UserProfileManagementDaoImpl getParticipantDetailsByEmail() - Ends ");
    return loginAttempt;
  }

  @Override
  public UserDetailsBO saveParticipant(UserDetailsBO participant) {
    logger.info("UserProfileManagementDaoImpl saveParticipant() - Starts ");
    Transaction transaction = null;
    CriteriaBuilder criteriaBuilder = null;
    CriteriaQuery<UserDetailsBO> criteriaQuery = null;
    Predicate[] predicates = new Predicate[1];
    Root<UserDetailsBO> userDetailsRoot = null;
    List<UserDetailsBO> userDetailsList = null;
    UserDetailsBO userDetailsBO = null;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      criteriaBuilder = session.getCriteriaBuilder();
      transaction = session.beginTransaction();
      criteriaQuery = criteriaBuilder.createQuery(UserDetailsBO.class);
      userDetailsRoot = criteriaQuery.from(UserDetailsBO.class);
      predicates[0] =
          criteriaBuilder.equal(
              userDetailsRoot.get(AppConstants.USER_DETAILS_ID), participant.getUserDetailsId());
      criteriaQuery.select(userDetailsRoot).where(predicates);
      userDetailsList = session.createQuery(criteriaQuery).getResultList();
      if (!userDetailsList.isEmpty()) {
        userDetailsBO = userDetailsList.get(0);
        userDetailsBO.setEmailCode(participant.getEmailCode());
        userDetailsBO.setCodeExpireDate(participant.getCodeExpireDate());
        session.update(userDetailsBO);
      }
      transaction.commit();
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl - saveParticipant() - error ", e);
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error(
              "UserProfileManagementDaoImpl - getUserDetailsForPasswordReset() - error rollback",
              e1);
        }
      }
    }
    logger.info("UserProfileManagementDaoImpl saveParticipant() - Ends ");
    return userDetailsBO;
  }

  @Override
  public void resetLoginAttempts(String email) {
    CriteriaBuilder criteriaBuilder = null;
    CriteriaDelete<LoginAttemptsBO> criteriaDelete = null;
    Root<LoginAttemptsBO> attemptRoot = null;
    Transaction transaction = null;
    logger.info("UserProfileManagementDaoImpl - resetLoginAttempts() - starts");
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      criteriaBuilder = session.getCriteriaBuilder();
      transaction = session.beginTransaction();
      criteriaDelete = criteriaBuilder.createCriteriaDelete(LoginAttemptsBO.class);
      attemptRoot = criteriaDelete.from(LoginAttemptsBO.class);
      criteriaDelete.where(criteriaBuilder.equal(attemptRoot.get("email"), email));
      session.createQuery(criteriaDelete).executeUpdate();
      transaction.commit();
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl - resetLoginAttempts() - error ", e);
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error("UserProfileManagementDaoImpl - resetLoginAttempts() - error rollback", e1);
        }
      }
    }

    logger.info("UserProfileManagementDaoImpl - resetLoginAttempts() - end");
  }

  @Override
  public UserDetailsBO getParticipantDetails(String userId) {
    logger.info("UserProfileManagementDaoImpl getParticipantDetails() - Starts ");
    UserDetailsBO userDetailsBO = null;
    CriteriaBuilder criteriaBuilder = null;
    CriteriaQuery<UserDetailsBO> criteriaQuery = null;
    Root<UserDetailsBO> userDetailsBoRoot = null;
    Predicate[] predicates = new Predicate[1];
    List<UserDetailsBO> userDetailsBoList = null;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      criteriaBuilder = session.getCriteriaBuilder();
      criteriaQuery = criteriaBuilder.createQuery(UserDetailsBO.class);
      userDetailsBoRoot = criteriaQuery.from(UserDetailsBO.class);
      predicates[0] = criteriaBuilder.equal(userDetailsBoRoot.get(AppConstants.KEY_USERID), userId);
      criteriaQuery.select(userDetailsBoRoot).where(predicates);
      userDetailsBoList = session.createQuery(criteriaQuery).getResultList();
      if (!userDetailsBoList.isEmpty()) {
        userDetailsBO = userDetailsBoList.get(0);
      }
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl getParticipantDetails() - error ", e);
    }
    logger.info("UserProfileManagementDaoImpl getParticipantDetails() - Ends ");
    return userDetailsBO;
  }

  @Override
  public boolean deActivateAcct(String userId, List<String> deleteData, Integer userDetailsId) {
    logger.info("UserProfileManagementDaoImpl deActivateAcct() - Starts ");
    Transaction transaction = null;
    CriteriaBuilder criteriaBuilder = null;

    CriteriaDelete<UserAppDetailsBO> criteriaUserAppDetailsDelete = null;
    Root<UserAppDetailsBO> userAppDetailsRoot = null;
    CriteriaDelete<AuthInfoBO> criteriaAuthInfoDelete = null;
    Root<AuthInfoBO> authInfoRoot = null;
    CriteriaUpdate<UserDetailsBO> criteriaUserDetailsUpdate = null;
    Root<UserDetailsBO> userDetailsRootUpdate = null;

    CriteriaUpdate<ParticipantStudiesBO> criteriaParticipantStudiesUpdate = null;
    Root<ParticipantStudiesBO> participantStudiesRoot = null;
    List<Predicate> studyIdPredicates = new ArrayList<>();
    Predicate[] studyInfoIdPredicates = new Predicate[1];
    Expression<String> studyIdExpression = null;
    Predicate[] predicatesAuthInfo = new Predicate[1];
    Predicate[] predicatesUserDetails = new Predicate[1];
    Predicate[] predicatesUserAppDetails = new Predicate[1];
    CriteriaQuery<StudyInfoBO> studyInfoQuery = null;
    Root<StudyInfoBO> rootStudyBO = null;
    List<StudyInfoBO> studyInfoBoList = null;
    List<Integer> studyInfoIdList = null;
    int isUpdated = 0;
    int count = 0;
    boolean returnVal = false;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      transaction = session.beginTransaction();
      criteriaBuilder = session.getCriteriaBuilder();
      if (deleteData != null && !deleteData.isEmpty()) {
        studyInfoQuery = criteriaBuilder.createQuery(StudyInfoBO.class);
        rootStudyBO = studyInfoQuery.from(StudyInfoBO.class);
        studyIdExpression = rootStudyBO.get("customId");
        studyInfoIdPredicates[0] = studyIdExpression.in(deleteData);
        studyInfoQuery.select(rootStudyBO).where(studyInfoIdPredicates);
        studyInfoBoList = session.createQuery(studyInfoQuery).getResultList();
        studyInfoIdList =
            studyInfoBoList.stream().map(StudyInfoBO::getId).collect(Collectors.toList());
        criteriaParticipantStudiesUpdate =
            criteriaBuilder.createCriteriaUpdate(ParticipantStudiesBO.class);
        participantStudiesRoot = criteriaParticipantStudiesUpdate.from(ParticipantStudiesBO.class);
        criteriaParticipantStudiesUpdate.set("status", "Withdrawn");
        criteriaParticipantStudiesUpdate.set("participantId", null);
        UserDetailsBO userDetails = session.get(UserDetailsBO.class, userDetailsId);
        studyIdPredicates.add(
            criteriaBuilder.equal(participantStudiesRoot.get("userDetails"), userDetails));
        studyIdExpression = participantStudiesRoot.get("studyInfo");
        studyIdPredicates.add(studyIdExpression.in(studyInfoBoList));
        criteriaParticipantStudiesUpdate.where(
            studyIdPredicates.toArray(new Predicate[studyIdPredicates.size()]));
        isUpdated = session.createQuery(criteriaParticipantStudiesUpdate).executeUpdate();
      }

      criteriaAuthInfoDelete = criteriaBuilder.createCriteriaDelete(AuthInfoBO.class);
      authInfoRoot = criteriaAuthInfoDelete.from(AuthInfoBO.class);
      predicatesAuthInfo[0] = criteriaBuilder.equal(authInfoRoot.get("userId"), userDetailsId);
      criteriaAuthInfoDelete.where(predicatesAuthInfo);
      session.createQuery(criteriaAuthInfoDelete).executeUpdate();

      criteriaUserAppDetailsDelete = criteriaBuilder.createCriteriaDelete(UserAppDetailsBO.class);
      userAppDetailsRoot = criteriaUserAppDetailsDelete.from(UserAppDetailsBO.class);
      predicatesUserAppDetails[0] =
          criteriaBuilder.equal(userAppDetailsRoot.get("userDetailsId"), userDetailsId);
      criteriaUserAppDetailsDelete.where(predicatesUserAppDetails);
      session.createQuery(criteriaUserAppDetailsDelete).executeUpdate();

      criteriaUserDetailsUpdate = criteriaBuilder.createCriteriaUpdate(UserDetailsBO.class);
      userDetailsRootUpdate = criteriaUserDetailsUpdate.from(UserDetailsBO.class);
      criteriaUserDetailsUpdate.set("status", 3);
      predicatesUserDetails[0] = criteriaBuilder.equal(userDetailsRootUpdate.get("userId"), userId);
      criteriaUserDetailsUpdate.where(predicatesUserDetails);
      count = session.createQuery(criteriaUserDetailsUpdate).executeUpdate();
      if (count > 0) {
        returnVal = true;
      }
      transaction.commit();
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl deActivateAcct() - error ", e);
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error("UserProfileManagementDaoImpl - deActivateAcct() - error rollback", e1);
        }
      }
    }
    logger.info("UserProfileManagementDaoImpl deActivateAcct() - Ends ");
    return returnVal;
  }

  @Override
  public AppInfoDetailsBO getAppPropertiesDetailsByAppId(Integer appId) {
    logger.info("UserProfileManagementDaoImpl - resetLoginAttempts() - starts");
    AppInfoDetailsBO appPropertiesDetails = null;
    CriteriaBuilder criteriaBuilder = null;
    CriteriaQuery<AppInfoDetailsBO> criteriaQuery = null;
    Root<AppInfoDetailsBO> appDetailsRoot = null;
    List<AppInfoDetailsBO> appPropetiesDetailList = null;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      criteriaBuilder = session.getCriteriaBuilder();
      criteriaQuery = criteriaBuilder.createQuery(AppInfoDetailsBO.class);
      appDetailsRoot = criteriaQuery.from(AppInfoDetailsBO.class);
      criteriaQuery.where(
          criteriaBuilder.equal(appDetailsRoot.get(AppConstants.APPLICATION_ID), appId));
      appPropetiesDetailList = session.createQuery(criteriaQuery).getResultList();

      if (!appPropetiesDetailList.isEmpty()) {
        appPropertiesDetails = appPropetiesDetailList.get(0);
      }
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl - resetLoginAttempts() - error ", e);
    }
    return appPropertiesDetails;
  }

  @Override
  public ErrorBean removeDeviceToken(int userId) {
    Transaction transaction = null;
    Query query = null;
    ErrorBean errorBean = null;
    logger.info("UserProfileManagementDaoImpl - removeDeviceToken() - starts");
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      transaction = session.beginTransaction();
      query =
          session.createQuery(
              "UPDATE AuthInfoBO ABO SET ABO.deviceToken=NULL WHERE ABO.userId=:userId");
      query.setParameter("userId", userId);
      int result = query.executeUpdate();
      errorBean =
          result > 0
              ? new ErrorBean(ErrorCode.EC_200.code(), ErrorCode.EC_200.errorMessage())
              : new ErrorBean(ErrorCode.EC_61.code(), ErrorCode.EC_61.errorMessage());
      transaction.commit();
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl - removeDeviceToken() - error ", e);
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error("UserProfileManagementDaoImpl - removeDeviceToken() - error rollback", e1);
        }
      }
      errorBean = new ErrorBean(ErrorCode.EC_500.code(), ErrorCode.EC_500.errorMessage());
    }
    logger.info("UserProfileManagementDaoImpl - removeDeviceToken() - end");
    return errorBean;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getStatesList() {
    List<String> statesList = null;
    logger.info("UserProfileManagementDaoImpl - getStatesList() - starts");
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      Query query =
          session.createQuery(
              "SELECT DISTINCT SIMBO.state FROM StateInstitutionMappingBO SIMBO ORDER BY SIMBO.state ASC");
      statesList = query.getResultList();
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl - getStatesList() - error ", e);
    }
    logger.info("UserProfileManagementDaoImpl - getStatesList() - end");
    return statesList;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getInstitutionsList(String state) {
    List<String> institutionsList = null;
    logger.info("UserProfileManagementDaoImpl - getInstitutionsList() - starts");
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      Query query =
          session.createQuery(
              "SELECT SIMBO.institutionId FROM StateInstitutionMappingBO SIMBO WHERE SIMBO.state=:state AND (SIMBO.newlyAdded=false OR SIMBO.newlyAdded IS NULL) AND (SIMBO.removed=false OR SIMBO.removed IS NULL) ORDER BY SIMBO.institutionId ASC");
      query.setParameter("state", state);
      institutionsList = query.getResultList();
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl - getInstitutionsList() - error ", e);
    }
    logger.info("UserProfileManagementDaoImpl - getInstitutionsList() - end");
    return institutionsList;
  }

  @Override
  public boolean updateNewlyAddedInstitutes(List<StateInstitutionMappingBO> newInstitutionList) {
    logger.info("UserProfileManagementDaoImpl - updateNewlyAddedInstitutes() - starts");
    Transaction transaction = null;
    boolean flag = false;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      transaction = session.beginTransaction();
      for (StateInstitutionMappingBO newInstitute : newInstitutionList) {
        newInstitute.setNewlyAdded(false);
        session.update(newInstitute);
      }
      transaction.commit();
      flag = true;
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl - updateNewlyAddedInstitutes() - error ", e);
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error(
              "UserProfileManagementDaoImpl - updateNewlyAddedInstitutes() - error rollback", e1);
        }
      }
    }
    logger.info("UserProfileManagementDaoImpl - updateNewlyAddedInstitutes() - end");
    return flag;
  }

  @Override
  public boolean removeInstitutions(List<StateInstitutionMappingBO> institutionToRemoveList) {
    logger.info("UserProfileManagementDaoImpl - removeInstitutions() - starts");
    Transaction transaction = null;
    boolean flag = false;
    try (Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
      transaction = session.beginTransaction();
      for (StateInstitutionMappingBO institution : institutionToRemoveList) {
        Query query =
            session.createQuery(
                "DELETE FROM UserInstitution UI WHERE UI.state=:state AND UI.institutionId=:institution");
        query.setParameter("state", institution.getState());
        query.setParameter("institution", institution.getInstitutionId());
        query.executeUpdate();

        institution.setRemoved(true);
        institution.setToRemove(false);
        session.update(institution);
      }
      transaction.commit();
      flag = true;
    } catch (Exception e) {
      logger.error("UserProfileManagementDaoImpl - removeInstitutions() - error ", e);
      if (transaction != null) {
        try {
          transaction.rollback();
        } catch (Exception e1) {
          logger.error("UserProfileManagementDaoImpl - removeInstitutions() - error rollback", e1);
        }
      }
    }
    logger.info("UserProfileManagementDaoImpl - removeInstitutions() - end");
    return flag;
  }
}

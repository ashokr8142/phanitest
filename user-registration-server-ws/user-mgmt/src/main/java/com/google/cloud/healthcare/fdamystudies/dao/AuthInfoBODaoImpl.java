/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.dao;

import com.google.cloud.healthcare.fdamystudies.beans.InstitutionAuthDetailsBean;
import com.google.cloud.healthcare.fdamystudies.exceptions.SystemException;
import com.google.cloud.healthcare.fdamystudies.model.AppInfoDetailsBO;
import com.google.cloud.healthcare.fdamystudies.model.AuthInfoBO;
import com.google.cloud.healthcare.fdamystudies.repository.AuthInfoBORepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserInstitutionRepository;
import com.google.cloud.healthcare.fdamystudies.util.AppConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthInfoBODaoImpl implements AuthInfoBODao {

  private static final Logger logger = LoggerFactory.getLogger(AuthInfoBODaoImpl.class);
  @Autowired AuthInfoBORepository authInfoRepository;

  @Autowired UserInstitutionRepository userInstitutionRepository;

  @Override
  public AuthInfoBO save(AuthInfoBO authInfo) throws SystemException {
    logger.info("AuthInfoBODaoImpl save() - starts");
    AuthInfoBO dbResponse = null;
    if (authInfo != null) {
      try {
        dbResponse = authInfoRepository.save(authInfo);
        logger.info("AuthInfoBODaoImpl save() - ends");
        return dbResponse;
      } catch (Exception e) {
        logger.error("AuthInfoBODaoImpl save(): ", e);
        throw new SystemException();
      }
    } else return null;
  }

  @Override
  public Map<String, JSONArray> getDeviceTokenOfAllUsers(List<AppInfoDetailsBO> appInfos) {
    logger.info("AuthInfoBODaoImpl.getDeviceTokenOfAllUsers()-Start");
    JSONArray androidJsonArray = null;
    JSONArray iosJsonArray = null;
    Map<String, JSONArray> deviceMap = new HashMap<>();
    try {
      List<Integer> appInfoIds =
          appInfos.stream().map(a -> a.getAppInfoId()).distinct().collect(Collectors.toList());

      if (appInfoIds != null && !appInfoIds.isEmpty()) {
        List<AuthInfoBO> authInfos = authInfoRepository.findDevicesTokens(appInfoIds);
        if (authInfos != null && !authInfos.isEmpty()) {
          androidJsonArray = new JSONArray();
          iosJsonArray = new JSONArray();
          for (AuthInfoBO authInfoBO : authInfos) {
            String devicetoken = authInfoBO.getDeviceToken();
            String devicetype = authInfoBO.getDeviceType();
            if (devicetoken != null && devicetype != null) {
              if (devicetype.equalsIgnoreCase(AppConstants.DEVICE_ANDROID)) {
                androidJsonArray.put(devicetoken.trim());
              } else if (devicetype.equalsIgnoreCase(AppConstants.DEVICE_IOS)) {
                iosJsonArray.put(devicetoken.trim());
              } else {
                logger.error("Invalid Device Type");
              }
            }
          }
          deviceMap.put(AppConstants.DEVICE_ANDROID, androidJsonArray);
          deviceMap.put(AppConstants.DEVICE_IOS, iosJsonArray);
        }
      }
    } catch (Exception e) {
      logger.error("AuthInfoBODaoImpl.getDeviceTokenOfAllUsers()-error", e);
    }
    logger.info("AuthInfoBODaoImpl.getDeviceTokenOfAllUsers()-end ");
    return deviceMap;
  }

  @Override
  public InstitutionAuthDetailsBean getDeviceTokenOfUsersForInstitutionAffiliation(
      List<AppInfoDetailsBO> appInfos, List<Integer> userIdsWithInstitutionAffiliation) {
    logger.info("AuthInfoBODaoImpl.getDeviceTokenOfUsersWithNoInstitutionAffiliation()-Start");
    JSONArray androidJsonArray = null;
    JSONArray iosJsonArray = null;
    Map<String, JSONArray> deviceMap = new HashMap<>();
    List<Integer> userIdList = new ArrayList<Integer>();
    InstitutionAuthDetailsBean institutionAuthDetailsBean = new InstitutionAuthDetailsBean();
    try {
      List<Integer> appInfoIds =
          appInfos.stream().map(a -> a.getAppInfoId()).distinct().collect(Collectors.toList());
      List<Integer> userIdsWithIntitution = null;
      if (userIdsWithInstitutionAffiliation == null) {
        userIdsWithIntitution =
            userInstitutionRepository
                .findAll()
                .stream()
                .map(userInstitution -> userInstitution.getUser().getUserDetailsId())
                .distinct()
                .collect(Collectors.toList());
      }

      if (appInfoIds != null && !appInfoIds.isEmpty()) {
        List<AuthInfoBO> authInfos =
            userIdsWithInstitutionAffiliation == null
                ? userIdsWithIntitution.isEmpty()
                    ? authInfoRepository.findDevicesTokens(appInfoIds)
                    : authInfoRepository.findDevicesTokensOfUsersWithNoInstitutionAffiliation(
                        appInfoIds, userIdsWithIntitution)
                : authInfoRepository.findDevicesTokensOfUsersWithInstitutionAffiliation(
                    appInfoIds, userIdsWithInstitutionAffiliation);
        if (authInfos != null && !authInfos.isEmpty()) {
          androidJsonArray = new JSONArray();
          iosJsonArray = new JSONArray();
          for (AuthInfoBO authInfoBO : authInfos) {
            userIdList.add(authInfoBO.getUserId());
            String devicetoken = authInfoBO.getDeviceToken();
            String devicetype = authInfoBO.getDeviceType();
            if (devicetoken != null && devicetype != null) {
              if (devicetype.equalsIgnoreCase(AppConstants.DEVICE_ANDROID)) {
                androidJsonArray.put(devicetoken.trim());
              } else if (devicetype.equalsIgnoreCase(AppConstants.DEVICE_IOS)) {
                iosJsonArray.put(devicetoken.trim());
              } else {
                logger.error("Invalid Device Type");
              }
            }
          }
          deviceMap.put(AppConstants.DEVICE_ANDROID, androidJsonArray);
          deviceMap.put(AppConstants.DEVICE_IOS, iosJsonArray);

          institutionAuthDetailsBean.setDeviceMap(deviceMap);
          institutionAuthDetailsBean.setUserIdList(userIdList);
        }
      }
    } catch (Exception e) {
      logger.error(
          "AuthInfoBODaoImpl.getDeviceTokenOfUsersWithNoInstitutionAffiliation()-error", e);
    }
    logger.info("AuthInfoBODaoImpl.getDeviceTokenOfUsersWithNoInstitutionAffiliation()-end ");
    return institutionAuthDetailsBean;
  }
}

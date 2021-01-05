/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import com.google.cloud.healthcare.fdamystudies.beans.ErrorBean;
import com.google.cloud.healthcare.fdamystudies.beans.SummaryReportBean;
import com.google.cloud.healthcare.fdamystudies.beans.UserResourceBean;
import com.google.cloud.healthcare.fdamystudies.beans.UserResourcesBean;
import com.google.cloud.healthcare.fdamystudies.service.PersonalizedUserReportService;
import com.google.cloud.healthcare.fdamystudies.util.AppUtil;
import com.google.cloud.healthcare.fdamystudies.util.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.util.GetUserInstitutionResources;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonalizedResourcesController {
  private static final Logger logger =
      LoggerFactory.getLogger(PersonalizedResourcesController.class);

  @Autowired PersonalizedUserReportService personalizedUserReportService;
  @Autowired GetUserInstitutionResources institutionResourcesService;

  @GetMapping(value = "/getPersonalizedResources")
  public UserResourcesBean getPersonalizedResources(
      @RequestHeader("userId") String userId,
      @RequestHeader("accessToken") String accessToken,
      @RequestHeader("clientToken") String clientToken,
      @RequestParam(name = "studyId") String studyId) {
    logger.info("UserResourcesController getPersonalizedResources() - starts");
    List<UserResourceBean> personalizedUserReports =
        personalizedUserReportService.getLatestPersonalizedUserReports(userId, studyId);
    List<UserResourceBean> institutionResources =
        institutionResourcesService.getInstitutionResourcesForUser(userId);
    logger.info("UserResourcesController getPersonalizedResources() - Ends");
    return new UserResourcesBean(
        Stream.concat(personalizedUserReports.stream(), institutionResources.stream())
            .collect(Collectors.toList()));
  }

  @PostMapping(
      value = "/savePersonalizedReports",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> savePersonalizedReports(
      @RequestHeader("participantId") String participantId,
      @RequestBody List<SummaryReportBean> summaryReports,
      @Context HttpServletResponse response) {
    logger.info("PersonalizedResourcesController savePersonalizedReports() - Starts");
    ErrorBean errorBean = null;
    if (org.apache.commons.lang3.StringUtils.isBlank(participantId)) {
      errorBean = new ErrorBean(ErrorCode.EC_711.code(), ErrorCode.EC_711.errorMessage());
      return new ResponseEntity<>(errorBean, HttpStatus.BAD_REQUEST);
    }
    try {
      errorBean =
          personalizedUserReportService.savePersonalizedReports(participantId, summaryReports);
      if (errorBean.getCode() == ErrorCode.EC_500.code()) {
        return new ResponseEntity<>(errorBean, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      logger.error("PersonalizedResourcesController savePersonalizedReports() - Error", e);
      return AppUtil.httpResponseForInternalServerError();
    }
    logger.info("PersonalizedResourcesController savePersonalizedReports() - Ends");
    return new ResponseEntity<>(errorBean, HttpStatus.OK);
  }
}

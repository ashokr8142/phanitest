/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.ErrorBean;
import com.google.cloud.healthcare.fdamystudies.beans.SummaryReportBean;
import com.google.cloud.healthcare.fdamystudies.beans.UserResourceBean;
import com.google.cloud.healthcare.fdamystudies.dao.PersonalizedUserReportDao;
import com.google.cloud.healthcare.fdamystudies.model.PersonalizedUserReportBO;
import com.google.cloud.healthcare.fdamystudies.repository.PersonalizedUserReportRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonalizedUserReportService {
  private static final Logger logger = LoggerFactory.getLogger(PersonalizedUserReportService.class);

  @Autowired PersonalizedUserReportRepository repository;
  @Autowired private PersonalizedUserReportDao personalizedUserReportDao;

  private static final UserResourceBean.ResourceType resourceType =
      UserResourceBean.ResourceType.PERSONALIZED_REPORT;

  public List<UserResourceBean> getLatestPersonalizedUserReports(String userId, String studyId) {
    return repository
        .findByUserDetailsUserIdAndStudyInfoCustomId(userId, studyId)
        .stream()
        .collect(
            Collectors.toMap(
                PersonalizedUserReportBO::getReportTitle,
                Function.identity(),
                BinaryOperator.maxBy(
                    Comparator.comparing(PersonalizedUserReportBO::getCreationTime))))
        .entrySet()
        .stream()
        .filter(e -> e.getValue().getCreationTime() != null)
        .sorted(
            Comparator.comparing(
                    e ->
                        ((Map.Entry<String, PersonalizedUserReportBO>) e)
                            .getValue()
                            .getCreationTime())
                .reversed())
        .map(
            e ->
                new UserResourceBean(
                    e.getKey(),
                    e.getValue().getReportContent(),
                    resourceType,
                    e.getValue().getId().toString()))
        .collect(Collectors.toList());
  }

  public ErrorBean savePersonalizedReports(
      String participantId, List<SummaryReportBean> summaryReports) {
    logger.info("PersonalizedUserReportService - savePersonalizedReports() - Starts");
    ErrorBean errorBean =
        personalizedUserReportDao.savePersonalizedReports(participantId, summaryReports);
    logger.info("PersonalizedUserReportService - savePersonalizedReports() - Ends");
    return errorBean;
  }
}

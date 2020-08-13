package com.google.cloud.healthcare.fdamystudies.controller;

import com.google.cloud.healthcare.fdamystudies.bean.ErrorBean;
import com.google.cloud.healthcare.fdamystudies.service.CommonService;
import com.google.cloud.healthcare.fdamystudies.service.ParticipantChartInfoService;
import com.google.cloud.healthcare.fdamystudies.utils.AppConstants;
import com.google.cloud.healthcare.fdamystudies.utils.AppUtil;
import com.google.cloud.healthcare.fdamystudies.utils.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParticipantChartController {
  @Autowired private ParticipantChartInfoService participantChartInfoService;

  @Autowired private CommonService commonService;

  private static final Logger logger = LoggerFactory.getLogger(ParticipantChartController.class);

  @GetMapping(
      value = "/participant/get-participant-chart",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<?> getParticipantChart(
      @RequestParam(name = "studyId") String studyId,
      @RequestParam("participantId") String participantId) {
    if (StringUtils.isBlank(studyId) || StringUtils.isBlank(participantId)) {
      ErrorBean errorBean =
          AppUtil.dynamicResponse(
              ErrorCode.EC_701.code(),
              ErrorCode.EC_701.errorMessage(),
              AppConstants.ERROR_STR,
              ErrorCode.EC_701.errorMessage());
      return new ResponseEntity<>(errorBean, HttpStatus.BAD_REQUEST);
    } else {
      try {
        String participantChart =
            participantChartInfoService.getParticpantChartAsHtml(participantId, studyId);
        return new ResponseEntity<>(participantChart, HttpStatus.OK);
      } catch (Exception e) {

        logger.error(
            "(C)...ParticipantChartController.getParticipantChart()...Exception " + e.getMessage());
        return new ResponseEntity<>(AppConstants.CHART_ERROR_HTML, HttpStatus.BAD_REQUEST);
      }
    }
  }
}

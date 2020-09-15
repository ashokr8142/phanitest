package com.google.cloud.healthcare.fdamystudies.controller;

import com.google.cloud.healthcare.fdamystudies.bean.ParticipantChartBean;
import com.google.cloud.healthcare.fdamystudies.service.ParticipantChartInfoService;
import com.google.cloud.healthcare.fdamystudies.utils.ProcessResponseException;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ParticipantChartController {
  @Autowired private ParticipantChartInfoService participantChartInfoService;

  private static final Logger logger = LoggerFactory.getLogger(ParticipantChartController.class);

  @RequestMapping("/participant/get-participant-chart")
  public String getParticipantChart(
      Model model,
      @RequestParam(name = "studyId") String studyId,
      @RequestParam("participantId") String participantId) {
    try {
      List<ParticipantChartBean> participantChartBeanList =
          participantChartInfoService.getParticipantChartBean(participantId, studyId);
      if (participantChartBeanList != null) {
        model.addAttribute("participantChartBeanList", participantChartBeanList);
        return "participant_chart";
      } else {

        return "participant_chart_error";
      }
    } catch (ProcessResponseException | IOException pe) {
      logger.error(
          "(C)...ParticipantChartController.getParticipantChart()...Exception " + pe.getMessage());
      return "participant_chart_error";
    }
  }
}

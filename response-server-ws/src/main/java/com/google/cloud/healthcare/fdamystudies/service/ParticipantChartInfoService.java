package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.bean.ParticipantChartBean;
import com.google.cloud.healthcare.fdamystudies.utils.ProcessResponseException;
import java.io.IOException;
import java.util.List;

public interface ParticipantChartInfoService {

  public List<ParticipantChartBean> getParticipantChartBean(String participantId, String studyId)
      throws IOException, ProcessResponseException;
}

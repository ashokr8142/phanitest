package com.google.cloud.healthcare.fdamystudies.service;

import java.io.IOException;

public interface ParticipantChartInfoService {
  String getParticpantChartAsHtml(String participantId, String studyId) throws IOException;
}

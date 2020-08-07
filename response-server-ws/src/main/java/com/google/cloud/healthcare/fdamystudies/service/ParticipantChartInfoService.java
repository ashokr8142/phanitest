package com.google.cloud.healthcare.fdamystudies.service;

public interface ParticipantChartInfoService {
  String getParticpantChartAsHtml(String participantId, String studyId);
}

package com.google.cloud.healthcare.fdamystudies.dao;

import com.google.cloud.healthcare.fdamystudies.beans.ErrorBean;
import com.google.cloud.healthcare.fdamystudies.beans.SummaryReportBean;
import java.util.List;

public interface PersonalizedUserReportDao {
  public ErrorBean savePersonalizedReports(
      String participantId, List<SummaryReportBean> summaryReports);
}

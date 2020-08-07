package com.google.cloud.healthcare.fdamystudies.repository;

import com.google.cloud.healthcare.fdamystudies.model.ParticipantChartInfoBo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantChartInfoBoRepository
    extends JpaRepository<ParticipantChartInfoBo, Integer> {
  List<ParticipantChartInfoBo> findByParticipantIdentifierAndStudyIdAndActivityIdOrderByCreatedAsc(
      String participantIdentifier, String studyId, String activityId);
}

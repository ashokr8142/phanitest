package com.google.cloud.healthcare.fdamystudies.repository;

import com.google.cloud.healthcare.fdamystudies.model.ParticipantChartInfoBo;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantChartInfoBoRepository
    extends JpaRepository<ParticipantChartInfoBo, Integer> {
  List<ParticipantChartInfoBo> findByParticipantIdentifierAndStudyIdAndActivityIdOrderByCreatedAsc(
      String participantIdentifier, String studyId, String activityId);

  @Query(
      "SELECT min(pc.created) from ParticipantChartInfoBo pc WHERE pc.participantIdentifier = :participantIdentifier and pc.studyId = :studyId")
  LocalDateTime findMinCreatedDateForParticipant(
      @Param("participantIdentifier") String participantIdentifier,
      @Param("studyId") String studyId);
}

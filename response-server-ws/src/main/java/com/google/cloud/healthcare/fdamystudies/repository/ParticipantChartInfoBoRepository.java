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

  @Query(
      "SELECT max(pc.created) from ParticipantChartInfoBo pc WHERE pc.participantIdentifier = :participantIdentifier and pc.studyId = :studyId")
  LocalDateTime findMaxCreatedDateForParticipant(
      @Param("participantIdentifier") String participantIdentifier,
      @Param("studyId") String studyId);

  @Query(
      "SELECT pc from ParticipantChartInfoBo pc WHERE pc.participantIdentifier = :participantIdentifier "
          + "and pc.studyId = :studyId "
          + "and pc.activityId = :activityId and pc.created >= :dateTimeStart and pc.created <= :dateTimeEnd "
          + "order by pc.created ASC")
  List<ParticipantChartInfoBo> findParticipantChartInfoBetweenDates(
      @Param("participantIdentifier") String participantIdentifier,
      @Param("studyId") String studyId,
      @Param("activityId") String activityId,
      @Param("dateTimeStart") LocalDateTime dateTimeStart,
      @Param("dateTimeEnd") LocalDateTime dateTimeEnd);
}

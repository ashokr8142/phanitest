package com.google.cloud.healthcare.fdamystudies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudiesBO;

@Repository
public interface ParticipantStudiesRepository extends JpaRepository<ParticipantStudiesBO, Integer> {
  public ParticipantStudiesBO findByParticipantId(String participantId);
}

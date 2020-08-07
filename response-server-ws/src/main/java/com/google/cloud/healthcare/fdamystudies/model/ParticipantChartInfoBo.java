package com.google.cloud.healthcare.fdamystudies.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "participant_chart_info")
public class ParticipantChartInfoBo implements Serializable {

  private static final long serialVersionUID = -8669517487080184697L;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @Column(name = "participant_identifier", nullable = false)
  private String participantIdentifier;

  @Column(name = "study_id", nullable = false)
  private String studyId;

  @Column(name = "activity_id", nullable = false)
  private String activityId;

  @Column(name = "question_id", nullable = false)
  private String questionId;

  @Column(name = "question_response")
  private String questionResponse;

  @Column(name = "created", columnDefinition = "TIMESTAMP")
  private LocalDateTime created;

  @Column(name = "created_by")
  private String createdBy;
}

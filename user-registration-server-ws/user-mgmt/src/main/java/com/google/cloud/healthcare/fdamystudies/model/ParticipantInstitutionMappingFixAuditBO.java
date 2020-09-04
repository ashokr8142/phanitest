package com.google.cloud.healthcare.fdamystudies.model;

import java.util.Date;
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
@Table(name = "participant_institution_mapping_fix_audit")
public class ParticipantInstitutionMappingFixAuditBO {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "participant_id")
  private String participantId;

  @Column(name = "current_institution_id")
  private String currentInstitutionId;

  @Column(name = "updated_institution_id")
  private String updatedInstitutionId;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "created_timestamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Date createdTimeStamp;
}

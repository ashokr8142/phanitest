package com.google.cloud.healthcare.fdamystudies.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(
    name = "state_institution_mapping",
    uniqueConstraints = @UniqueConstraint(columnNames = {"state", "institution_id"}))
public class StateInstitutionMappingBO {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "state")
  private String state;

  @Column(name = "institution_id")
  private String institutionId;
  
  @Column(name = "newly_added")
  private Boolean newlyAdded;
  
  @Column(name = "to_remove")
  private Boolean toRemove;
  
  @Column(name = "removed")
  private Boolean removed;
}

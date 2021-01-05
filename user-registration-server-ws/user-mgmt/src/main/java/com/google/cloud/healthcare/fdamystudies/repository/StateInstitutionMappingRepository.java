package com.google.cloud.healthcare.fdamystudies.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.google.cloud.healthcare.fdamystudies.model.StateInstitutionMappingBO;

@Repository
public interface StateInstitutionMappingRepository
    extends JpaRepository<StateInstitutionMappingBO, Integer> {
  
  public List<StateInstitutionMappingBO> findByNewlyAdded(Boolean newlyAdded);
  
  public List<StateInstitutionMappingBO> findByToRemove(Boolean toRemove);
}

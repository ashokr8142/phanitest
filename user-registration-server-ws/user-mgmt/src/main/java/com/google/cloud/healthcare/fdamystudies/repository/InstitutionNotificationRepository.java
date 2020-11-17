package com.google.cloud.healthcare.fdamystudies.repository;

import com.google.cloud.healthcare.fdamystudies.model.InstitutionNotificationBO;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionNotificationRepository
    extends JpaRepository<InstitutionNotificationBO, Integer> {

  @Query(
      "SELECT INBO FROM InstitutionNotificationBO INBO WHERE INBO.appId=(?1) AND INBO.userId=(?2) ORDER BY createdTime DESC")
  public List<InstitutionNotificationBO> findByAppIdAndUserId(
      String appId, Integer userId, Pageable pageable);
}

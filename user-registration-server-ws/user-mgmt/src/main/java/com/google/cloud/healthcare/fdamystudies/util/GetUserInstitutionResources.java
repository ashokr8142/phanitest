package com.google.cloud.healthcare.fdamystudies.util;

import com.google.cloud.healthcare.fdamystudies.beans.UserResourceBean;
import com.google.cloud.healthcare.fdamystudies.model.UserInstitution;
import com.google.cloud.healthcare.fdamystudies.repository.UserInstitutionRepository;
import com.google.cloud.healthcare.fdamystudies.service.CloudStorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetUserInstitutionResources {
  @Autowired UserInstitutionRepository userInstitutionRepository;

  @Autowired CloudStorageService cloudStorageService;

  private static final UserResourceBean.ResourceType resourceType =
      UserResourceBean.ResourceType.INSTITUTION_RESOURCE;

  // Returns UserResourceBeans for the institution that `userId` belongs to.
  // Can be an empty list.
  public List<UserResourceBean> getInstitutionResourcesForUser(String userId) {
    Optional<UserInstitution> maybeUserInstitution =
        userInstitutionRepository.findByUserUserId(userId);
    List<CloudStorageService.InstitutionResource> streams = null;
    if (maybeUserInstitution.isPresent()) {
      UserInstitution userInstitution = maybeUserInstitution.get();
      streams = cloudStorageService.getAllInstitutionResources(userInstitution.getInstitutionId());
      if (streams.isEmpty()) {
        streams = cloudStorageService.getAllInstitutionResources(AppConstants.GENERIC_INSTITUTION);
      }
    } else {
      streams = cloudStorageService.getAllInstitutionResources(AppConstants.GENERIC_INSTITUTION);
    }

    if (streams.isEmpty()) return new ArrayList<>();

    List<UserResourceBean> resources = new ArrayList<>();
    for (CloudStorageService.InstitutionResource institutionResource : streams) {
      String content = new String(institutionResource.stream.toByteArray());
      resources.add(
          new UserResourceBean(
              institutionResource.title, content, resourceType, institutionResource.hash));
    }
    return resources;
  }
}

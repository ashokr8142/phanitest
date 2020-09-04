package com.google.cloud.healthcare.fdamystudies.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantInstitutionMappingFixAuditBO;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudiesBO;
import com.google.cloud.healthcare.fdamystudies.model.UserDetailsBO;
import com.google.cloud.healthcare.fdamystudies.model.UserInstitution;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantInstitutionMappingFixAuditRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudiesRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserDetailsBORepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserInstitutionRepository;
import com.google.cloud.healthcare.fdamystudies.util.UserManagementUtil;

@RestController
public class InstitutionController {
  @Resource private ParticipantStudiesRepository participantStudiesRepository;
  @Resource private UserInstitutionRepository userInstitutionRepository;
  @Resource private UserDetailsBORepository userDetailsBORepository;

  @Resource
  private ParticipantInstitutionMappingFixAuditRepository
      participantInstitutionMappingFixAuditRepository;

  @GetMapping("/updateInstitution")
  public ResponseEntity<?> updateInstitution() {
    // get all participantids
    List<String> participants = new ArrayList<>();
    try {
      ClassPathResource resource = new ClassPathResource("participants.txt");
      InputStream inputStream = resource.getInputStream();
      Scanner sc = new Scanner(inputStream);
      while (sc.hasNextLine()) {
        participants.add(sc.nextLine());
      }
      sc.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // get userId of each participants and store in a map
    HashMap<String, Integer> participantUserIdMap = new HashMap<>();
    for (String participant : participants) {
      ParticipantStudiesBO participantsBo =
          participantStudiesRepository.findByParticipantId(participant);
      if (participantsBo != null) {
        participantUserIdMap.put(participant, participantsBo.getUserDetails().getUserDetailsId());
      }
    }
    // get participants with institutionId
    HashMap<String, String> institution = new HashMap<>();
    try {
      ClassPathResource resource = new ClassPathResource("institution.txt");
      InputStream inputStream = resource.getInputStream();
      Scanner sc = new Scanner(inputStream);
      while (sc.hasNextLine()) {
        String[] str = sc.nextLine().split(",");
        institution.put(str[0], str[1]);
      }
      sc.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // compare and insert
    for (String participantId : participantUserIdMap.keySet()) {
      String currentInstitutionId = null;
      String updatedInstitutionId = null;
      UserInstitution userInstitution =
          userInstitutionRepository.findByUserUserDetailsId(
              participantUserIdMap.get(participantId));
      if (userInstitution == null) {
        updatedInstitutionId = institution.get(participantId);
        UserDetailsBO userDetailsBO =
            userDetailsBORepository.findByUserDetailsId(participantUserIdMap.get(participantId));
        UserInstitution userInstitutionBo = new UserInstitution();
        userInstitutionBo.setUser(userDetailsBO);
        userInstitutionBo.setInstitutionId(updatedInstitutionId);
        userInstitutionRepository.save(userInstitutionBo);
      } else {
        if (StringUtils.isBlank(userInstitution.getInstitutionId())) {
          currentInstitutionId = userInstitution.getInstitutionId();
          updatedInstitutionId = institution.get(participantId);
          userInstitution.setInstitutionId(updatedInstitutionId);
          userInstitutionRepository.save(userInstitution);
        } else {
          currentInstitutionId = userInstitution.getInstitutionId();
          updatedInstitutionId = "NOT_UPDATED";
        }
      }
      ParticipantInstitutionMappingFixAuditBO participantInstitutionMappingFixAuditBO =
          new ParticipantInstitutionMappingFixAuditBO();
      participantInstitutionMappingFixAuditBO.setParticipantId(participantId);
      participantInstitutionMappingFixAuditBO.setCurrentInstitutionId(currentInstitutionId);
      participantInstitutionMappingFixAuditBO.setUpdatedInstitutionId(updatedInstitutionId);
      participantInstitutionMappingFixAuditBO.setCreatedBy("user_reg_server");
      participantInstitutionMappingFixAuditBO.setCreatedTimeStamp(
          UserManagementUtil.getCurrentUtilDateTime());
      participantInstitutionMappingFixAuditRepository.save(participantInstitutionMappingFixAuditBO);
    }
    boolean flag =
        participantInstitutionMappingFixAuditRepository.count() == participants.size()
            ? true
            : false;
    return new ResponseEntity<>(flag, HttpStatus.OK);
  }
}

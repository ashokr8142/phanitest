package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRequestBeanV2 {
  private ProfileRespBean profile;
  private SettingsRespBean settings;
  private InfoBean info;
  private List<ParticipantInfoBean> participantInfo;
  private InstitutionInfoBean institutionInfo;
}

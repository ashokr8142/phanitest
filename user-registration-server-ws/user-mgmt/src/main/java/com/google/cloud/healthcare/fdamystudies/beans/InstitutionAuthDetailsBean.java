package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;

@Setter
@Getter
public class InstitutionAuthDetailsBean {
  private Map<String, JSONArray> deviceMap;
  private List<Integer> userIdList;
}

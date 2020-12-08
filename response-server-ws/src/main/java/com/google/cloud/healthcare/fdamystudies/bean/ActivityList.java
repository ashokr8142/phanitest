package com.google.cloud.healthcare.fdamystudies.bean;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ActivityList{
    public String activityId;
    public List<String> questionIds;
}

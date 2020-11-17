package com.google.cloud.healthcare.fdamystudies.model;

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
@Table(name = "institution_notification")
public class InstitutionNotificationBO {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "app_id")
  private String appId;

  @Column(name = "notification_text")
  private String notificationText;

  @Column(name = "notification_type")
  private String notificationType;

  @Column(name = "notification_subtype")
  private String notificationSubType;

  @Column(name = "created_time")
  private String createdTime;

  @Column(name = "user_id")
  private Integer userId;
}

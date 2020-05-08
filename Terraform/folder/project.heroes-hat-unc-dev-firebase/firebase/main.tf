# Copyright 2020 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This folder contains Terraform resources to setup a Google Cloud Firebase instance. It enables
# Firebase resources on the given GCP project.

terraform {
  required_version = "~> 0.12.0"
  required_providers {
    google      = "~> 3.0"
    google-beta = "~> 3.0"
  }
  backend "gcs" {}
}

resource "google_firebase_project" "firebase" {
  provider = google-beta
  project  = var.project_id
}

resource "google_firebase_project_location" "basic" {
  provider = google-beta
  project  = google_firebase_project.firebase.project

  location_id = "us-east1"
}

# Firestore data export
module "my_studies_firestore_data_bucket" {
  source  = "terraform-google-modules/cloud-storage/google//modules/simple_bucket"
  version = "~> 1.4"

  name        = "heroes-hat-unc-dev-my-studies-firestore-raw-data"
  project_id  = var.project_id
  location    = var.storage_location
  iam_members = var.firestore_data_bucket_iam_members

  # TTL 7 days.
  lifecycle_rules = [{
    action = {
      type = "Delete"
    }
    condition = {
      age        = 7 # 7 days
      with_state = "ANY"
    }
  }]
}

module "survey_pubsub" {
  source  = "terraform-google-modules/pubsub/google"
  version = "~> 1.2.1"

  topic      = "surveyWriteTrigger"
  project_id = var.project_id
  pull_subscriptions = [
    {
      name                 = "surveyPHQDep"
      ack_deadline_seconds = 10
    }
  ]
}

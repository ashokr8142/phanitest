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

terraform {
  required_version = "~> 0.12.0"
  required_providers {
    google      = "~> 3.0"
    google-beta = "~> 3.0"
  }
  backend "gcs" {}
}

# Create a separate service account for each cloud function.
locals {
  functions = [
    "raw-data-export",
    "bigquery-export",
    "real-time-triggers",
  ]
}

resource "google_service_account" "functions_service_accounts" {
  for_each = toset(local.functions)

  account_id  = "${each.key}-fn"
  description = "Terraform-generated service account for use by the ${each.key} Cloud Function"
  project     = var.project_id
}

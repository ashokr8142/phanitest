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

resource "google_project_iam_member" "datastore_users" {
  for_each = toset(var.datastore_users)
  project  = var.project_id
  role     = "roles/datastore.user"
  member   = each.key
}

resource "google_project_iam_member" "datastore_import_export_admins" {
  for_each = toset(var.datastore_import_export_admins)
  project  = var.project_id
  role     = "roles/datastore.importExportAdmin"
  member   = each.key
}

resource "google_project_iam_member" "pubsub_subscribers" {
  for_each = toset(var.pubsub_subscribers)
  project  = var.project_id
  role     = "roles/pubsub.subscriber"
  member   = each.key
}

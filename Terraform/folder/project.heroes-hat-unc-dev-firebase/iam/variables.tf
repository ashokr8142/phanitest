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

variable "project_id" {
  type = string
}

variable "datastore_users" {
  description = "Clients who have access to the firestore in this project"
  type        = list(string)
}

variable "datastore_import_export_admins" {
  description = "Clients who have importExportAdmins access to the firestore in this project"
  type        = list(string)
}

variable "pubsub_subscribers" {
  description = "Pubsub subscribers in this project"
  type        = list(string)
}

# Copyright 2020 Google LLC
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

variable "allowed_policy_member_customer_ids" {
  description = "The list of Cloud Identity or G Suite customer IDs corresponding to the domains to allow users from. Must be specified."
  default     = []
}

variable "allowed_public_vms" {
  description = "The list of Compute Engine instances that are allowed direct external access (i.e. with an external IP). Default to deny all if not specified. Supported entry format: projects/PROJECT_ID/zones/ZONE/instances/INSTANCE."
  default     = []
}

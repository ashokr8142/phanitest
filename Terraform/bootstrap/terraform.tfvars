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

devops_project_id = "heroes-hat-unc-dev-devops"
folder_id            = "455887492777"
billing_account   = "00936C-CC8624-429851"
state_bucket      = "heroes-hat-unc-dev-terraform-state"
storage_location  = "us-east1"
#org_admin         = "group:roachjm@unc.edu"
devops_owners = [
  "group:heroes-health-devops-owners@gcp.unc.edu",
  "serviceAccount:1061179896581-compute@developer.gserviceaccount.com",
]

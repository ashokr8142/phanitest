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

folder                        = "455887492777"
devops_project_id             = "heroes-hat-unc-dev-devops"
state_bucket                  = "heroes-hat-unc-dev-terraform-state"
repo_owner                    = "roachjm-unc"
repo_name                     = "fda-mystudies-unc"
branch_regex                  = "^master$"
continuous_deployment_enabled = true
trigger_enabled               = false
terraform_root                = "Terraform"
build_viewers = [
  "group:heroes-health-approvers@gcp.unc.edu",
]

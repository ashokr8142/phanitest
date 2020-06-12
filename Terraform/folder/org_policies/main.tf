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

# This folder contains Terraform resources to configure GCP Organization Policies.
# (https://cloud.google.com/resource-manager/docs/organization-policy/org-policy-constraints)
# See the following resources for the details of policies enforced.
#
# To apply the policies, the Cloud Build Service Account needs to be granted the Organization level
# roles/orgpolicy.policyAdmin permission (https://cloud.google.com/iam/docs/understanding-roles#organization-policy-roles).
# Once the permission is granted, uncomment the file and let the Cloud Build job apply the policies.

# terraform {
#   required_version = "~> 0.12.0"
#   required_providers {
#     google      = "~> 3.0"
#     google-beta = "~> 3.0"
#   }
#   backend "gcs" {}
# }

# # Cloud SQL
# module "orgpolicy_sql_restrict_authorized_networks" {
#   source  = "terraform-google-modules/org-policy/google"
#   version = "~> 3.0.2"

#   policy_for = "folder"
#   folder_id  = "455887492777"

#   constraint  = "constraints/sql.restrictAuthorizedNetworks"
#   policy_type = "boolean"
#   enforce     = true
# }

# module "orgpolicy_sql_restrict_public_ip" {
#   source  = "terraform-google-modules/org-policy/google"
#   version = "~> 3.0.2"

#   policy_for = "folder"
#   folder_id  = "455887492777"

#   constraint  = "constraints/sql.restrictPublicIp"
#   policy_type = "boolean"
#   enforce     = true
# }

# # Compute Engine
# module "orgpolicy_compute_disable_nested_virtualization" {
#   source  = "terraform-google-modules/org-policy/google"
#   version = "~> 3.0.2"

#   policy_for = "folder"
#   folder_id  = "455887492777"

#   constraint  = "constraints/compute.disableNestedVirtualization"
#   policy_type = "boolean"
#   enforce     = true
# }

# module "orgpolicy_compute_disable_serial_port_access" {
#   source  = "terraform-google-modules/org-policy/google"
#   version = "~> 3.0.2"

#   policy_for = "folder"
#   folder_id  = "455887492777"

#   constraint  = "constraints/compute.disableSerialPortAccess"
#   policy_type = "boolean"
#   enforce     = true
# }

# module "orgpolicy_compute_skip_default_network_creation" {
#   source  = "terraform-google-modules/org-policy/google"
#   version = "~> 3.0.2"

#   policy_for = "folder"
#   folder_id  = "455887492777"

#   constraint  = "constraints/compute.skipDefaultNetworkCreation"
#   policy_type = "boolean"
#   enforce     = true
# }

# module "orgpolicy_compute_vm_external_ip_access" {
#   source  = "terraform-google-modules/org-policy/google"
#   version = "~> 3.0.2"

#   policy_for = "folder"
#   folder_id  = "455887492777"

#   constraint        = "constraints/compute.vmExternalIpAccess"
#   policy_type       = "list"
#   allow             = var.allowed_public_vms
#   allow_list_length = length(var.allowed_public_vms)
# }

# module "orgpolicy_compute_restrict_xpn_project_lien_removal" {
#   source  = "terraform-google-modules/org-policy/google"
#   version = "~> 3.0.2"

#   policy_for = "folder"
#   folder_id  = "455887492777"

#   constraint  = "constraints/compute.restrictXpnProjectLienRemoval"
#   policy_type = "boolean"
#   enforce     = true
# }

# # Cloud Identity and Access Management
# # Uncomment once filling in the `allowed_policy_member_customer_ids` in terraform.tfvars for your
# # gsuite domain. Obtain the ID by following
# # https://cloud.google.com/resource-manager/docs/organization-policy/restricting-domains#retrieving_customer_id.
# # module "orgpolicy_iam_allowed_policy_member_domains" {
# #   source  = "terraform-google-modules/org-policy/google"
# #   version = "~> 3.0.2"

# #   policy_for = "folder"
# #   folder_id = "455887492777"

# #   constraint        = "constraints/iam.allowedPolicyMemberDomains"
# #   policy_type       = "list"
# #   allow             = var.allowed_policy_member_customer_ids
# #   allow_list_length = length(var.allowed_policy_member_customer_ids)
# # }

# # Google Cloud Platform - Resource Locations
# module "orgpolicy_gcp_resource_locations" {
#   source  = "terraform-google-modules/org-policy/google"
#   version = "~> 3.0.2"

#   policy_for = "folder"
#   folder_id  = "455887492777"

#   constraint        = "constraints/gcp.resourceLocations"
#   policy_type       = "list"
#   allow             = ["in:us-locations"]
#   allow_list_length = 1
# }

# # Cloud Storage
# # Uncomment if you want to enforce uniform bucket-level access.
# # https://cloud.google.com/storage/docs/org-policy-constraints#uniform-access
# # module "orgpolicy_storage_uniform_bucket_level_access" {
# #   source  = "terraform-google-modules/org-policy/google"
# #   version = "~> 3.0.2"

# #   policy_for = "folder"
# #   folder_id  = "455887492777"

# #   constraint  = "constraints/storage.uniformBucketLevelAccess"
# #   policy_type = "boolean"
# #   enforce     = true
# # }

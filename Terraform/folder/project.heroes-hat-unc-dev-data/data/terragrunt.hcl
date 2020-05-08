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

include {
  path = find_in_parent_folders()
}

dependency "project" {
  config_path  = "../project"
  skip_outputs = true
}

dependency "network" {
  config_path = "../../project.heroes-hat-unc-dev-networks/networks"

  mock_outputs = {
    private_network = {
      id = "projects/mock/global/networks/mock-network"
    }
  }
}

dependency "apps" {
  config_path = "../../project.heroes-hat-unc-dev-apps/apps"

  mock_outputs = {
    service_account = "mock-gke-service-account"
    apps_service_accounts = {
      user-registration = {
        email = "mock-app-gke@mock-project.iam.gserviceaccount.com"
      }
    }
  }
}

dependency "functions" {
  config_path = "../../project.heroes-hat-unc-dev-firebase/functions"

  mock_outputs = {
    functions_service_accounts = {
      user-registration = {
        email = "mock-function-fn@mock-project.iam.gserviceaccount.com"
      }
    }
  }
}

inputs = {
  network = dependency.network.outputs.private_network.id
  consent_documents_iam_members = [{
    role   = "roles/storage.objectAdmin"
    member = "serviceAccount:${dependency.apps.outputs.apps_service_accounts["user-registration"].email}"
  }]
  fda_resources_iam_members = [{
    role   = "roles/storage.objectAdmin"
    member = "serviceAccount:${dependency.apps.outputs.apps_service_accounts["study-designer"].email}"
  }]
  firestore_data_bucket_iam_members = [{
    role   = "roles/storage.objectCreator"
    member = "serviceAccount:${dependency.functions.outputs.functions_service_accounts["raw-data-export"].email}"
  },
{
    role   = "roles/storage.objectViewer"
    member = "serviceAccount:${dependency.functions.outputs.functions_service_accounts["bigquery-export"].email}"
  }]
  firestore_data_bigquery_access = [{
    role = "roles/bigquery.dataEditor"
    user_by_email = "${dependency.functions.outputs.functions_service_accounts["bigquery-export"].email}"
  }]
}

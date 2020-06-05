# Heroes Health data processing repository
This repository contains code of data processing components for Heroes Health project.

# Overview
There are 4 components for data processing:
1. Firestore raw data export

2. BigQuery export

3. Firestore triggers on survey write event

4. PubSub consumer

# Set Up

## Prework
1. Clone the Git repository: `git clone <GIT_REPO_URL>`

2. Install firebase tool (If that gives you error, follow the setup of npm instruction [here](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)):
`npm install -g firebase-tools`

## Firestore raw data export
1. Create a storage bucket where the data will be exported ([instructions](https://cloud.google.com/storage/docs/creating-buckets)). Make sure you choose the location close to your Firestore data.
> NOTE: The storage bucket can also be created by the Terraform config.
2. `cd firestore/raw_data_export`.
3. Edit `.firebaserc` to change default to the GCP project holding the Firestore.
4. Run `firebase login` (or `firebase login --no-localhost` if running on a remote machine).
5. Run `firebase init`, choose `Functions` from the list to initialize and agree to install dependencies.
6. Edit `deploy_function.sh` and change `BUCKET_NAME` to match the storage bucket created in Step#1.
7. Run `./deploy_function.sh` to deploy the Firebase function.

You can see the logs/manage the jobs in the Firebase functions console and the Cloud scheduler console. The latter lets you trigger run instantly by clicking the RUN NOW button.

## BigQuery export
1. `cd firestore/bigquery_export`.
2. Edit `.firebaserc` to change default to the GCP project holding the Firestore.
3. Run `firebase login`
4. Run `firebase init`, choose `Functions` from the list to initialize and agree to install dependencies.
5. Create a BigQuery data set (substitute project id).

        bq --location=US mk -d <PROJECT_ID>:firestoreRawData
> NOTE: This manual step can be skipped if the BigQuery data set is created by Terraform config.

6. Edit `deploy_function.sh` and change the following environment variables:
   1. `BUCKET_NAME` to match the storage bucket created in "Firestore raw data export".
   2. `BQ_PROJECT_ID` to match the `PROJECT_ID` in Step#5.
7. Run `./deploy_function.sh` to deploy the Firebase function.

The export will happen whenever a write is completed on the raw export data from the Firestore. The data is then available to query with the BigQuery console.

## Firestore triggers on survey write event
1. Create a PubSub topic to which the real time trigger will publish:

        gcloud pubsub topics create surveyWriteTrigger

> NOTE: This manual step can be skipped if the PubSub topic is created by Terraform config.

2. `cd firestore/real_time_triggers`.
3. Run `firebase login`
4. Run `firebase init`, choose `Functions` from the list to initialize and agree to install dependencies.
5. Get the name of the PubSub topic you created by running `gcloud pubsub topics list`
and get the one ending with `surveyWriteTrigger`.
6. Edit `deploy_function.sh` and change `PUBSUB_TOPIC` to match the topic name retrieved in Step#5.
7. Run `./deploy_function.sh` to deploy the Firebase function.

The PubSub massage will be published on each survey submission. Each contains activityId (e.g. `PHQDep`), participantId and associated data.

## PubSub consumer
1. Setup service account `triggers-pubsub-handler` with following permissions:
    * CloudSQL Client
    * Pub/Sub Subscriber
    * Storage Object Viewer
2. Create a MySQL db user in the MyStudies GCP project.
3. Create a subscription to the `surveyWriteTrigger` topic. Assuming `PROJECT_ID` is set to your project:

        gcloud pubsub subscriptions create surveyWriteGlobal --topic=surveyWriteTrigger --topic-project=$PROJECT_ID 

4. Create new kubernetes cluster (example name `triggers-pubsub-cluster`) with service account set to `triggers-pubsub-handler`.
5. Go to the folder `firestore/real_time_triggers/subscriber`. Change the values in `deployment.yaml` to reflect the service configuration. The list of values to change:
    * CLOUD_SQL_CONNECTION_NAME
    * PROJECT_ID
    * DB_NAME, DB_PASS, DB_USER secret,
    * secrets-volume secret,
    * instances in cloudsql-proxy configuration,
    * docker image path

6. You will need to create the secrets mentioned above. Here are example commands for the 3 secrets needed. Note that the arguments will be different for you.

        kubectl create secret generic triggers-handler-db-credentials --from-literal=username=triggers-pubsub --from-literal=password=triggers-pb-pass --from-literal=dbname=mystudies_userregistration

        kubectl create secret generic triggers-handler-gcloud-key --from-file=sql_credentials.json=/Users/aspy/Documents/heroes-hat-dev-triggers-pubsub-handler-key.json

      For the last one you get the key for the triggers-pubsub-handler service account. You can set it in the same UI as you created the service account or use the command:

        gcloud iam service-accounts keys create ~/key.json --iam-account "triggers-pubsub-handler@$PROJECT_ID.iam.gserviceaccount.com"

7. Build the image:

        gcloud builds submit --tag gcr.io/$PROJECT_ID/triggers-pubsub-subscriber

8. Connect to the cluster:

        gcloud container clusters get-credentials triggers-pubsub-cluster

9. Create deployment:

        kubectl apply -f deployment.yaml


10. You can see if it was deployed properly by inspecting the pods.

        kubectl get all
        kubectl describe pod <podname>
        kubectl logs <podname>

11. You can update the image with:

        kubectl set image deployment/triggers-pubsub-deployment triggers-pubsub-subscriber=<image_path>

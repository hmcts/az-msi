# Azure (aad) pod identity POC


- Deploy pod identity infra:

`kubectl create -f https://raw.githubusercontent.com/Azure/aad-pod-identity/master/deploy/infra/deployment.yaml`

The previous command works for non-rbac clusters. For rbac clusters and more info see:
[aad-pod-identity](https://github.com/Azure/aad-pod-identity)


- Create resource group:

`az group create --location uksouth -g ccd-etl-v1`


- Create blob storage account and containers:

`az storage account create --name ccdetlacc1 -g ccd-etl-v1`

`az storage container create --name bsc1 --account-name ccdetlacc1` 

`az storage container create --name bsc2 --account-name ccdetlacc1`


- Create writer and reader identities:

`az identity create --name "ccd-etl-writer-test1" --resource-group ccd-etl-v1`

`az identity create --name "ccd-etl-reader-test1" --resource-group ccd-etl-v1`


- Assign roles

`az role assignment create --assignee "<writer-id>" --role "Storage Blob Data Contributor" \
  --scope "/subscriptions/<subscription-id>/resourceGroups/ccd-etl-v1/providers/Microsoft.Storage/storageAccounts/ccdetlacc1"`

`az role assignment create --assignee "<reader-id>" --role "Storage Blob Data Reader"  \
  --scope "/subscriptions/<subscription-id>/resourceGroups/ccd-etl-v1/providers/Microsoft.Storage/storageAccounts/ccdetlacc1/blobServices/default/containers/bsc1"`


- Show roles

`az identity list --resource-group ccd-etl-v1`


Use for each identity the id and clientId to configure the pod identity (ResourceID and ClientID). 
For more info see: [aad-pod-identity](https://github.com/Azure/aad-pod-identity)

- Deploy pod identity and binding to the cluster (see kubernetes folder in this repo).

- Deploy the application making sure the selector label on the pod match the identity binding selector.


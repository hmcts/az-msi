package uk.gov.hmcts.reform.azmsi;

import com.microsoft.azure.msiAuthTokenProvider.AzureMSICredentialException;
import com.microsoft.azure.msiAuthTokenProvider.MSICredentials;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsToken;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class BlobStorage {

    private static final String STORAGE_RESOURCE = "https://storage.azure.com/";
    private static final String BLOB_TPL = "https://%s.blob.core.windows.net/%s/%s";



    private StorageCredentials getCredentials(String clientId, String accountName) throws IOException, AzureMSICredentialException {
        MSICredentials credsProvider = MSICredentials.getMSICredentials();
        credsProvider.updateClientId(clientId);
        System.out.println("MSICredentials: " + credsProvider);
        String accessToken = credsProvider.getToken(STORAGE_RESOURCE).accessToken();
        System.out.println("Access token: " + accessToken);
        return new StorageCredentialsToken(accountName, accessToken);
    }

    private void write(String clientId, String accountName, String blobUri, String blobContent) throws URISyntaxException, StorageException, IOException, AzureMSICredentialException {
        StorageCredentials storageCredentials = getCredentials(clientId, accountName);
        System.out.println("Storage Credentials: " + storageCredentials);
        CloudBlockBlob blob = new CloudBlockBlob(new URI(blobUri), storageCredentials);
        blob.uploadText(blobContent);
    }

    private void read(String clientId, String accountName, String blobUri, String blobContent) throws URISyntaxException, StorageException, IOException, AzureMSICredentialException {
        StorageCredentials storageCredentials = getCredentials(clientId, accountName);
        System.out.println("Storage Credentials: " + storageCredentials);
        CloudBlockBlob blob = new CloudBlockBlob(new URI(blobUri), storageCredentials);
        String content = blob.downloadText();
        assert content.equals(blobContent);
    }


    public static void main(String[] args) {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        String clientId = System.getenv("MSI_CLIENT_ID");
        System.out.println("clientId: " + clientId);
        String operation = System.getenv("MSI_BLOB_OPERATION");
        System.out.println("operation: " + operation);
        String accountName = System.getenv("MSI_BLOB_ACCOUNT");
        System.out.println("accountName: " + accountName);
        String containerName = System.getenv("MSI_BLOB_CONTAINER");
        System.out.println("containerName: " + containerName);
        String blobName = System.getenv("MSI_BLOB_FILE");
        System.out.println("blobName: " + blobName);
        String blobContent = System.getenv("MSI_BLOB_CONTENT");
        System.out.println("blobContent: " + blobContent);

        String blobUri = String.format(BLOB_TPL, accountName, containerName, blobName);
        System.out.println("blobUri: " + blobUri);

        BlobStorage bt = new BlobStorage();
        OperationContext.setLoggingEnabledByDefault(true);
        try {
            if (operation.equalsIgnoreCase("write")) {
                bt.write(clientId, accountName, blobUri, blobContent);
            } else if (operation.equalsIgnoreCase("read")) {
                bt.read(clientId, accountName, blobUri, blobContent);
            } else {
                throw new RuntimeException("Please select either 'read' or 'write' operation.");
            }
            // just to leave the pod around for a while
            Thread.sleep(300_000L);
        } catch (URISyntaxException | StorageException | IOException | InterruptedException | AzureMSICredentialException e) {
            throw new RuntimeException(e);
        }
    }

}

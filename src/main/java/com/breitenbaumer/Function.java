package com.breitenbaumer;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.breitenbaumer.shared.BlobClientProvider;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    private Logger logger;

    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it
     * using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("blobStorageUpload")
    public HttpResponseMessage run(
      @HttpTrigger(name = "req", 
        methods = { HttpMethod.POST }, 
        authLevel = AuthorizationLevel.FUNCTION,
        dataType = "binary") 
      HttpRequestMessage<Optional<byte[]>> request,
      final ExecutionContext context) throws Exception {
    
        logger = context.getLogger();
    // start file upload
    logger.info("Java HTTP file upload started with headers " + request.getHeaders());
    
    // here the "content-type" must be lower-case
    byte[] bs = request.getBody().get();
    String fileName = getFileName(request.getHeaders());
    upload(bs, fileName);
    // return response
    logger.info("Java HTTP file upload ended. Length: " + bs.length);
    return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + bs.length).build();
  }

  // extracts file name from a multipart boundary
  public static String getFileName(Map<String,String> headers) {
    if(headers.containsKey("FileName")){
      return headers.get("FileName");
    }
    else{
      return "image_" + LocalDate.now().getYear() + "-" + LocalDate.now().getMonthValue() + "-" + LocalDate.now().getDayOfMonth() + "_" + LocalDateTime.now().getHour() + "-" + LocalDateTime.now().getMinute() + "-" + LocalDateTime.now().getSecond() + ".jpg";
    }
  }

    public void upload(byte[] content, String fileName) {
        try {
            BlobClientProvider provider = new BlobClientProvider(logger);
            BlobServiceClient blobServiceClient = provider.getBlobServiceClient();
            BlobContainerClient container = blobServiceClient.createBlobContainerIfNotExists(System.getenv("CONTAINERNAME"));
            BlobClient blobClient = provider.getBlobClient(container.getBlobContainerName(), fileName);
            logger.info("\n\tUpload a file as a block blob.");
            blobClient.upload(BinaryData.fromBytes(content), true);
            logger.info("\t\tSuccessfully uploaded the blob.");
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Java HTTP trigger processed a request.", e.fillInStackTrace());
        }
    }
}
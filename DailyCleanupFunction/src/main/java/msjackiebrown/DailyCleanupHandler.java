package msjackiebrown;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DailyCleanupHandler {

    private static final Logger logger = LoggerFactory.getLogger(DailyCleanupHandler.class);

    // Create an S3 client
    S3Client s3 = S3Client.create();

    public String handleRequest() {
        try {
            logger.info("Starting daily cleanup process...");
            
            // Read environment variables
            String bucketName = System.getenv("BUCKET_NAME");
            String daysEnv = System.getenv("DAYS");

            // Validate environment variables
            if (bucketName == null || bucketName.isEmpty()) {
                return "Environment variable BUCKET_NAME is not set.";
            }
            if (!bucketName.matches("^[a-zA-Z0-9.-]{3,63}$")) {
                return "Invalid BUCKET_NAME. Ensure it follows S3 bucket naming conventions.";
            }
            if (daysEnv == null || daysEnv.isEmpty()) {
                return "Environment variable DAYS is not set.";
            }

            // Parse the days value
            int days;
            try {
                days = Integer.parseInt(daysEnv);
            } catch (NumberFormatException e) {
                return "Environment variable DAYS must be a valid integer.";
            }
            if (days <= 0) {
                return "Environment variable DAYS must be a positive integer.";
            }

            // Calculate the cutoff time based on the current time minus the specified days
            Instant cutoffTime = Instant.now().minus(days, ChronoUnit.DAYS);

            // Create a request to list objects in the bucket
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            // Fetch the list of objects
            ListObjectsV2Response listObjectsResponse = s3.listObjectsV2(listObjectsRequest);

            // Build a response string with details of deleted objects
            StringBuilder response = new StringBuilder("Deleted objects from bucket " + bucketName + ":\n");
            boolean filesDeleted = false;

            for (S3Object s3Object : listObjectsResponse.contents()) {
                // Check if the object is older than the specified cutoff time
                if (s3Object.lastModified().isBefore(cutoffTime)) {
                    // Delete the object
                    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build();
                    s3.deleteObject(deleteObjectRequest);

                    // Append the deleted object's key to the response
                    response.append(s3Object.key()).append("\n");
                    filesDeleted = true;
                }
            }

            if (!filesDeleted) {
                response.append("No files to delete.");
            }

            logger.info("Cleanup completed successfully.");
            
            // Log the response
            logger.info("Response: {}", response.toString());
            
            return response.toString();
        } catch (Exception e) {
            logger.error("Error during cleanup process: {}", e.getMessage(), e);
            return "Error processing request: " + e.getMessage();
        }
    }
}

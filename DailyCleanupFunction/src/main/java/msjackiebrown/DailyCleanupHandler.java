package msjackiebrown;

import msjackiebrown.helpers.S3ClientHelper;
import msjackiebrown.helpers.SnsClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sns.SnsClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DailyCleanupHandler {

    private static final Logger logger = LoggerFactory.getLogger(DailyCleanupHandler.class);

    private final S3ClientHelper s3Helper;
    private final SnsClientHelper snsHelper;

    public DailyCleanupHandler() {
        S3Client s3Client = S3Client.create();
        SnsClient snsClient = SnsClient.create();
        this.s3Helper = new S3ClientHelper(s3Client);
        this.snsHelper = new SnsClientHelper(snsClient);
    }

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

            // Calculate the cutoff time
            Instant cutoffTime = Instant.now().minus(days, ChronoUnit.DAYS);

            // List and delete objects
            List<S3Object> objects = s3Helper.listObjects(bucketName);
            StringBuilder response = new StringBuilder("Deleted objects from bucket " + bucketName + ":\n");
            boolean filesDeleted = false;

            for (S3Object s3Object : objects) {
                if (s3Object.lastModified().isBefore(cutoffTime)) {
                    s3Helper.deleteObject(bucketName, s3Object.key());
                    response.append(s3Object.key()).append("\n");
                    filesDeleted = true;
                }
            }

            if (!filesDeleted) {
                response.append("No files to delete.");
            }

            logger.info("Cleanup completed successfully.");
            logger.info("Response: {}", response.toString());
            return response.toString();

        } catch (Exception e) {
            logger.error("Error during cleanup process: {}", e.getMessage(), e);

            // Send an SNS notification
            String snsTopicArn = System.getenv("SNS_TOPIC_ARN");
            snsHelper.sendNotification(snsTopicArn, "Daily Cleanup Job Failed", "Error during cleanup process: " + e.getMessage());

            return "Error processing request: " + e.getMessage();
        }
    }
}

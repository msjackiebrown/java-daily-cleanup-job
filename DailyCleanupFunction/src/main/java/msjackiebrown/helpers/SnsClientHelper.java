package msjackiebrown.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

public class SnsClientHelper {

    private static final Logger logger = LoggerFactory.getLogger(SnsClientHelper.class);
    private final SnsClient snsClient;

    public SnsClientHelper(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void sendNotification(String topicArn, String subject, String message) {
        if (topicArn == null || topicArn.isEmpty()) {
            logger.warn("SNS_TOPIC_ARN is not set. Skipping SNS notification.");
            return;
        }

        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject)
                    .message(message)
                    .build();
            PublishResponse publishResponse = snsClient.publish(publishRequest);
            logger.info("SNS notification sent. Message ID: {}", publishResponse.messageId());
        } catch (Exception e) {
            logger.error("Failed to send SNS notification: {}", e.getMessage(), e);
        }
    }
}
# Daily Cleanup Job

This project demonstrates how to use **Amazon EventBridge (formerly CloudWatch Events)** to invoke an AWS Lambda function to delete old files from an S3 bucket.

---

## Project Overview

- **Use Case**: Automatically delete files older than a specified number of days from an S3 bucket every day at midnight.
- **Lambda Function**: Scans the S3 bucket, identifies old files, and deletes them.
- **Trigger**: An EventBridge rule triggers the Lambda function daily at midnight (UTC).

---

## Features

- **Configurable Cleanup**: Set the number of days for determining old files via an environment variable (`DAYS`).
- **S3 Integration**: Uses the AWS SDK to interact with S3 for listing and deleting objects.
- **EventBridge Scheduling**: Automatically triggers the cleanup function at a specified time.
- **IAM Role**: Includes a least-privilege IAM role for accessing the S3 bucket.

---

## Project Structure

The project is organized as follows:

```
java-daily-cleanup-job/
├── README.md                # Project documentation
├── template.yaml            # AWS SAM template for deployment
├── samconfig.toml           # SAM CLI configuration
├── events/                  # Sample events for local testing
│   └── event.json
├── DailyCleanupFunction/    # Lambda function source code
│   ├── pom.xml              # Maven configuration for the function
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── msjackiebrown/
│   │   │           └── DailyCleanupHandler.java
│   │   └── test/
│   │       └── java/
│   │           └── msjackiebrown/
│   │               └── DailyCleanupHandlerTest.java
│   └── target/              # Build artifacts
├── .aws-sam/                # SAM build artifacts
│   ├── build/
│   └── cache/
└── .vscode/                 # VS Code configuration
    └── launch.json
```

---

## Architecture

The architecture of this project is as follows:

1. **AWS Lambda**: The core of the application, responsible for performing the cleanup task. It is triggered by an EventBridge rule.
2. **Amazon EventBridge**: Schedules the Lambda function to run daily at midnight (UTC).
3. **Amazon S3**: The storage service where files are stored and cleaned up based on the specified retention period.
4. **IAM Role**: Grants the Lambda function permissions to interact with the S3 bucket and write logs to CloudWatch.

### Workflow

1. The EventBridge rule triggers the Lambda function daily at midnight.
2. The Lambda function reads the `BUCKET_NAME` and `DAYS` environment variables.
3. It lists objects in the specified S3 bucket and deletes files older than the specified number of days.
4. Logs are written to CloudWatch for monitoring and debugging.

---

## Prerequisites

Before deploying this project, ensure you have the following:

1. **AWS CLI**: Installed and configured with appropriate permissions.
2. **AWS SAM CLI**: Installed for building and deploying the application.
3. **Java Development Kit (JDK)**: Version 21 or higher.
4. **Maven**: For building the Java project.
5. **S3 Bucket**: An existing S3 bucket where the cleanup will be performed.

---

## Deployment Instructions

### Step 1: Clone the Repository
```bash
git clone https://github.com/msjackiebrown/java-daily-cleanup-job.git
cd java-daily-cleanup-job
```

### Step 2: Build the Lambda Function
Navigate to the `DailyCleanupFunction` directory and build the project using Maven:
```bash
cd DailyCleanupFunction
mvn clean package
```

This will generate a deployable JAR file in the `target` directory.

### Step 3: Deploy the Application
Use the AWS SAM CLI to deploy the application:
```bash
sam deploy --guided
```

During the guided deployment, you will be prompted to provide:
- A stack name.
- The S3 bucket for deployment artifacts.
- Parameters such as the bucket name (`BUCKET_NAME`) and the number of minutes (`MINUTES`).

### Step 4: Verify the Deployment
After deployment, verify that:
- The Lambda function is created.
- The EventBridge rule is set to trigger the function daily at midnight.
- The IAM role has the necessary permissions to access the S3 bucket.

---

## Configuration

### Environment Variables
The Lambda function uses the following environment variables:
- **`BUCKET_NAME`**: The name of the S3 bucket to clean up.
- **`DAYS`**: The number of days to determine which files are considered old.

### EventBridge Rule
The EventBridge rule is configured to trigger the Lambda function daily at midnight (UTC) using the following cron expression:
```cron
cron(0 0 * * ? *)
```

---

## Testing

You can test the Lambda function locally using the AWS SAM CLI without providing an event:
```bash
sam local invoke DailyCleanUpFunction
```

The function will execute using the environment variables and logic defined in the code.

---

## Cleanup

To delete the deployed resources, run:
```bash
sam delete
```

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

### Author
**msjackiebrown**


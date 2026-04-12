package dev.nameless.poc.lambda.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class LambdaDeployService {

    private final S3Client s3Client;
    private final String artifactsBucket;

    public LambdaDeployService(
            S3Client s3Client,
            @Value("${aws.lambda.artifacts-bucket}") String artifactsBucket) {
        this.s3Client = s3Client;
        this.artifactsBucket = artifactsBucket;
    }

    public Map<String, String> uploadArtifact(String key, byte[] zipBytes) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(artifactsBucket)
                        .key(key)
                        .contentType("application/zip")
                        .build(),
                RequestBody.fromBytes(zipBytes));

        return Map.of(
                "bucket", artifactsBucket,
                "key", key,
                "size", String.valueOf(zipBytes.length));
    }

    public byte[] createSampleHandlerZip(String handlerCode) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("handler.py"));
            zos.write(handlerCode.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    public byte[] createEchoHandlerZip() throws IOException {
        String pythonHandler = """
                import json

                def handler(event, context):
                    print(f"Received event: {json.dumps(event)}")
                    return {
                        "statusCode": 200,
                        "body": json.dumps({
                            "message": "Hello from Lambda!",
                            "event": event
                        })
                    }
                """;
        return createSampleHandlerZip(pythonHandler);
    }

    public byte[] createS3TriggerHandlerZip() throws IOException {
        String pythonHandler = """
                import json

                def handler(event, context):
                    records = event.get("Records", [])
                    processed = []
                    for record in records:
                        bucket = record["s3"]["bucket"]["name"]
                        key = record["s3"]["object"]["key"]
                        processed.append({"bucket": bucket, "key": key})
                        print(f"Processing S3 event: s3://{bucket}/{key}")
                    return {
                        "statusCode": 200,
                        "body": json.dumps({"processed": processed})
                    }
                """;
        return createSampleHandlerZip(pythonHandler);
    }

    public byte[] createSqsTriggerHandlerZip() throws IOException {
        String pythonHandler = """
                import json

                def handler(event, context):
                    records = event.get("Records", [])
                    processed = []
                    for record in records:
                        body = record.get("body", "")
                        message_id = record.get("messageId", "")
                        processed.append({"messageId": message_id, "body": body})
                        print(f"Processing SQS message: {message_id}")
                    return {
                        "statusCode": 200,
                        "body": json.dumps({
                            "batchItemFailures": [],
                            "processed": len(processed)
                        })
                    }
                """;
        return createSampleHandlerZip(pythonHandler);
    }

    public String getArtifactsBucket() {
        return artifactsBucket;
    }
}

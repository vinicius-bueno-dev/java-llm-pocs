package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.Map;

@Service
public class MultipartUploadService {

    private final S3Client s3Client;

    public MultipartUploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String initiateMultipartUpload(String bucket, String key, String contentType) {
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build());
        return response.uploadId();
    }

    public String uploadPart(String bucket, String key, String uploadId,
                             int partNumber, byte[] data) {
        UploadPartResponse response = s3Client.uploadPart(
                UploadPartRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .build(),
                RequestBody.fromBytes(data));
        return response.eTag();
    }

    public String completeMultipartUpload(String bucket, String key, String uploadId,
                                          List<CompletedPart> parts) {
        CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .uploadId(uploadId)
                        .multipartUpload(CompletedMultipartUpload.builder()
                                .parts(parts)
                                .build())
                        .build());
        return response.eTag();
    }

    public void abortMultipartUpload(String bucket, String key, String uploadId) {
        s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .build());
    }

    public List<Map<String, String>> listMultipartUploads(String bucket) {
        return s3Client.listMultipartUploads(ListMultipartUploadsRequest.builder()
                .bucket(bucket)
                .build())
                .uploads().stream()
                .map(u -> Map.of(
                        "key", u.key(),
                        "uploadId", u.uploadId(),
                        "initiated", u.initiated().toString()))
                .toList();
    }
}

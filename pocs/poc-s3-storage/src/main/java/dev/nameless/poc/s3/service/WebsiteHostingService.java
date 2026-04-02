package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Map;

@Service
public class WebsiteHostingService {

    private final S3Client s3Client;

    public WebsiteHostingService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void configureWebsiteHosting(String bucket, String indexDoc, String errorDoc) {
        s3Client.putBucketWebsite(PutBucketWebsiteRequest.builder()
                .bucket(bucket)
                .websiteConfiguration(WebsiteConfiguration.builder()
                        .indexDocument(IndexDocument.builder().suffix(indexDoc).build())
                        .errorDocument(ErrorDocument.builder().key(errorDoc).build())
                        .build())
                .build());
    }

    public Map<String, String> getWebsiteConfiguration(String bucket) {
        GetBucketWebsiteResponse response = s3Client.getBucketWebsite(
                GetBucketWebsiteRequest.builder().bucket(bucket).build());
        return Map.of(
                "indexDocument", response.indexDocument().suffix(),
                "errorDocument", response.errorDocument().key());
    }

    public void deleteWebsiteConfiguration(String bucket) {
        s3Client.deleteBucketWebsite(DeleteBucketWebsiteRequest.builder()
                .bucket(bucket)
                .build());
    }

    public void uploadWebsiteFiles(String bucket) {
        String indexHtml = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"><title>POC S3 Website</title></head>
                <body>
                  <h1>S3 Static Website Hosting</h1>
                  <p>Esta pagina e servida diretamente do S3 via LocalStack.</p>
                </body>
                </html>
                """;
        String errorHtml = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"><title>Erro</title></head>
                <body>
                  <h1>404 — Pagina nao encontrada</h1>
                  <p>O recurso solicitado nao existe neste bucket.</p>
                </body>
                </html>
                """;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket).key("index.html")
                        .contentType("text/html").build(),
                RequestBody.fromString(indexHtml));

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket).key("error.html")
                        .contentType("text/html").build(),
                RequestBody.fromString(errorHtml));
    }
}

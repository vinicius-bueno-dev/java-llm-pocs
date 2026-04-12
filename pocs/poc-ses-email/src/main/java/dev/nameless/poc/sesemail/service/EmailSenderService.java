package dev.nameless.poc.sesemail.service;

import dev.nameless.poc.sesemail.dto.SendEmailDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.BulkEmailDestination;
import software.amazon.awssdk.services.ses.model.BulkEmailDestinationStatus;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendBulkTemplatedEmailRequest;
import software.amazon.awssdk.services.ses.model.SendBulkTemplatedEmailResponse;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Envia emails via SES usando diferentes estrategias: texto simples,
 * HTML, raw (com attachment via MIME manual) e bulk templated.
 */
@Service
public class EmailSenderService {

    private final SesClient sesClient;
    private final String fromEmail;

    public EmailSenderService(SesClient sesClient,
                              @Value("${aws.ses.from-email}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    /**
     * Envia um email simples com corpo em texto puro.
     */
    public String sendEmail(SendEmailDto dto) {
        SendEmailResponse response = sesClient.sendEmail(SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                        .toAddresses(dto.to())
                        .build())
                .message(Message.builder()
                        .subject(content(dto.subject()))
                        .body(Body.builder()
                                .text(content(dto.bodyText()))
                                .build())
                        .build())
                .build());
        return response.messageId();
    }

    /**
     * Envia um email com corpo HTML (e fallback em texto).
     */
    public String sendHtmlEmail(SendEmailDto dto) {
        Body.Builder bodyBuilder = Body.builder();
        if (dto.bodyText() != null) {
            bodyBuilder.text(content(dto.bodyText()));
        }
        if (dto.bodyHtml() != null) {
            bodyBuilder.html(content(dto.bodyHtml()));
        }

        SendEmailResponse response = sesClient.sendEmail(SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                        .toAddresses(dto.to())
                        .build())
                .message(Message.builder()
                        .subject(content(dto.subject()))
                        .body(bodyBuilder.build())
                        .build())
                .build());
        return response.messageId();
    }

    /**
     * Envia um email raw com attachment. O MIME e montado manualmente
     * via concatenacao de strings, sem dependencia de javax.mail.
     *
     * @param to             destinatario
     * @param subject        assunto
     * @param bodyText       corpo em texto
     * @param attachmentName nome do arquivo anexo
     * @param attachmentData conteudo do anexo em bytes
     * @param contentType    MIME type do anexo (ex.: application/pdf)
     */
    public String sendRawEmail(String to, String subject, String bodyText,
                               String attachmentName, byte[] attachmentData, String contentType) {
        String boundary = "----=_Part_" + UUID.randomUUID().toString().replace("-", "");

        StringBuilder raw = new StringBuilder();
        raw.append("From: ").append(fromEmail).append("\r\n");
        raw.append("To: ").append(to).append("\r\n");
        raw.append("Subject: ").append(subject).append("\r\n");
        raw.append("MIME-Version: 1.0\r\n");
        raw.append("Content-Type: multipart/mixed; boundary=\"").append(boundary).append("\"\r\n");
        raw.append("\r\n");

        // Parte texto
        raw.append("--").append(boundary).append("\r\n");
        raw.append("Content-Type: text/plain; charset=UTF-8\r\n");
        raw.append("Content-Transfer-Encoding: 7bit\r\n");
        raw.append("\r\n");
        raw.append(bodyText).append("\r\n");

        // Parte attachment
        raw.append("--").append(boundary).append("\r\n");
        raw.append("Content-Type: ").append(contentType).append("; name=\"").append(attachmentName).append("\"\r\n");
        raw.append("Content-Disposition: attachment; filename=\"").append(attachmentName).append("\"\r\n");
        raw.append("Content-Transfer-Encoding: base64\r\n");
        raw.append("\r\n");
        raw.append(Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(attachmentData));
        raw.append("\r\n");

        // Fechamento
        raw.append("--").append(boundary).append("--\r\n");

        SendRawEmailResponse response = sesClient.sendRawEmail(SendRawEmailRequest.builder()
                .rawMessage(RawMessage.builder()
                        .data(SdkBytes.fromByteArray(raw.toString().getBytes(StandardCharsets.UTF_8)))
                        .build())
                .build());
        return response.messageId();
    }

    /**
     * Envia emails em massa usando um template SES previamente criado.
     * Cada destinatario recebe o email com os dados de substituicao fornecidos.
     */
    public List<BulkEmailDestinationStatus> sendBulkEmail(String templateName,
                                                           String defaultTemplateData,
                                                           List<BulkEmailDestination> destinations) {
        SendBulkTemplatedEmailResponse response = sesClient.sendBulkTemplatedEmail(
                SendBulkTemplatedEmailRequest.builder()
                        .source(fromEmail)
                        .template(templateName)
                        .defaultTemplateData(defaultTemplateData)
                        .destinations(destinations)
                        .build());
        return response.status();
    }

    private Content content(String data) {
        return Content.builder()
                .data(data)
                .charset("UTF-8")
                .build();
    }
}

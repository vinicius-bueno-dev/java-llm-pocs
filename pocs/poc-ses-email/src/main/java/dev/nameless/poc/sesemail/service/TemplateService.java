package dev.nameless.poc.sesemail.service;

import dev.nameless.poc.sesemail.dto.SendTemplatedEmailDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.CreateTemplateRequest;
import software.amazon.awssdk.services.ses.model.DeleteTemplateRequest;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.GetTemplateRequest;
import software.amazon.awssdk.services.ses.model.GetTemplateResponse;
import software.amazon.awssdk.services.ses.model.ListTemplatesRequest;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailResponse;
import software.amazon.awssdk.services.ses.model.Template;
import software.amazon.awssdk.services.ses.model.TemplateMetadata;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gerencia templates SES e envia emails templated.
 * Templates permitem reutilizar layouts de email com variaveis
 * de substituicao (ex.: {{name}}, {{orderId}}).
 */
@Service
public class TemplateService {

    private final SesClient sesClient;
    private final String fromEmail;

    public TemplateService(SesClient sesClient,
                           @Value("${aws.ses.from-email}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    /**
     * Cria um template SES com nome, assunto, corpo HTML e corpo texto.
     */
    public void createTemplate(String templateName, String subject, String htmlBody, String textBody) {
        sesClient.createTemplate(CreateTemplateRequest.builder()
                .template(Template.builder()
                        .templateName(templateName)
                        .subjectPart(subject)
                        .htmlPart(htmlBody)
                        .textPart(textBody)
                        .build())
                .build());
    }

    /**
     * Retorna os detalhes de um template pelo nome.
     */
    public Template getTemplate(String templateName) {
        GetTemplateResponse response = sesClient.getTemplate(GetTemplateRequest.builder()
                .templateName(templateName)
                .build());
        return response.template();
    }

    /**
     * Lista todos os templates cadastrados.
     */
    public List<TemplateMetadata> listTemplates() {
        return sesClient.listTemplates(ListTemplatesRequest.builder().build())
                .templatesMetadata();
    }

    /**
     * Remove um template pelo nome.
     */
    public void deleteTemplate(String templateName) {
        sesClient.deleteTemplate(DeleteTemplateRequest.builder()
                .templateName(templateName)
                .build());
    }

    /**
     * Envia um email usando um template existente.
     * O templateData e convertido para JSON para substituicao de variaveis.
     */
    public String sendTemplatedEmail(SendTemplatedEmailDto dto) {
        String templateDataJson = toJson(dto.templateData());

        SendTemplatedEmailResponse response = sesClient.sendTemplatedEmail(
                SendTemplatedEmailRequest.builder()
                        .source(fromEmail)
                        .destination(Destination.builder()
                                .toAddresses(dto.to())
                                .build())
                        .template(dto.templateName())
                        .templateData(templateDataJson)
                        .build());
        return response.messageId();
    }

    /**
     * Converte um Map para JSON simples. Suficiente para template data
     * do SES sem precisar de dependencia externa (Jackson ja esta no classpath
     * via spring-boot-starter-web, mas construimos manualmente para clareza).
     */
    private String toJson(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return "{}";
        }
        String entries = data.entrySet().stream()
                .map(e -> "\"" + escapeJson(e.getKey()) + "\":\"" + escapeJson(e.getValue()) + "\"")
                .collect(Collectors.joining(","));
        return "{" + entries + "}";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

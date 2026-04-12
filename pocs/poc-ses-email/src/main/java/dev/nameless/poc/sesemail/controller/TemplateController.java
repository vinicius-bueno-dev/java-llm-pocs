package dev.nameless.poc.sesemail.controller;

import dev.nameless.poc.sesemail.dto.SendTemplatedEmailDto;
import dev.nameless.poc.sesemail.service.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.ses.model.Template;
import software.amazon.awssdk.services.ses.model.TemplateMetadata;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ses/templates")
public class TemplateController {

    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    /**
     * Cria um template SES.
     * Body esperado: { "templateName": "...", "subject": "...", "htmlBody": "...", "textBody": "..." }
     */
    @PostMapping
    public ResponseEntity<Void> createTemplate(@RequestBody Map<String, String> body) {
        service.createTemplate(
                body.get("templateName"),
                body.get("subject"),
                body.get("htmlBody"),
                body.get("textBody"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{templateName}")
    public ResponseEntity<Template> getTemplate(@PathVariable String templateName) {
        return ResponseEntity.ok(service.getTemplate(templateName));
    }

    @GetMapping
    public ResponseEntity<List<TemplateMetadata>> listTemplates() {
        return ResponseEntity.ok(service.listTemplates());
    }

    @DeleteMapping("/{templateName}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String templateName) {
        service.deleteTemplate(templateName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Envia um email usando um template existente.
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendTemplatedEmail(@RequestBody SendTemplatedEmailDto dto) {
        String messageId = service.sendTemplatedEmail(dto);
        return ResponseEntity.ok(Map.of("messageId", messageId));
    }
}

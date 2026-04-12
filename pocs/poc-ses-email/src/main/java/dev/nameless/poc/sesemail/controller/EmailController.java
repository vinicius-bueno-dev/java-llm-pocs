package dev.nameless.poc.sesemail.controller;

import dev.nameless.poc.sesemail.dto.SendEmailDto;
import dev.nameless.poc.sesemail.service.EmailSenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ses/emails")
public class EmailController {

    private final EmailSenderService service;

    public EmailController(EmailSenderService service) {
        this.service = service;
    }

    /**
     * Envia email simples (texto puro).
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody SendEmailDto dto) {
        String messageId = service.sendEmail(dto);
        return ResponseEntity.ok(Map.of("messageId", messageId));
    }

    /**
     * Envia email HTML (com fallback em texto).
     */
    @PostMapping("/send-html")
    public ResponseEntity<Map<String, String>> sendHtmlEmail(@RequestBody SendEmailDto dto) {
        String messageId = service.sendHtmlEmail(dto);
        return ResponseEntity.ok(Map.of("messageId", messageId));
    }

    /**
     * Envia email raw com attachment via multipart form.
     */
    @PostMapping("/send-raw")
    public ResponseEntity<Map<String, String>> sendRawEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String bodyText,
            @RequestParam MultipartFile attachment) throws IOException {
        String contentType = attachment.getContentType() != null
                ? attachment.getContentType()
                : "application/octet-stream";
        String messageId = service.sendRawEmail(
                to, subject, bodyText,
                attachment.getOriginalFilename(),
                attachment.getBytes(),
                contentType);
        return ResponseEntity.ok(Map.of("messageId", messageId));
    }
}

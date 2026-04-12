package dev.nameless.poc.sesemail.controller;

import dev.nameless.poc.sesemail.service.IdentityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.ses.model.IdentityVerificationAttributes;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ses/identities")
public class IdentityController {

    private final IdentityService service;

    public IdentityController(IdentityService service) {
        this.service = service;
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestBody Map<String, String> body) {
        service.verifyEmailIdentity(body.get("email"));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<String>> listIdentities() {
        return ResponseEntity.ok(service.listIdentities());
    }

    @GetMapping("/verification")
    public ResponseEntity<Map<String, IdentityVerificationAttributes>> getVerificationAttributes(
            @RequestParam List<String> identities) {
        return ResponseEntity.ok(service.getIdentityVerificationAttributes(identities));
    }

    @DeleteMapping("/{identity}")
    public ResponseEntity<Void> deleteIdentity(@PathVariable String identity) {
        service.deleteIdentity(identity);
        return ResponseEntity.noContent().build();
    }
}

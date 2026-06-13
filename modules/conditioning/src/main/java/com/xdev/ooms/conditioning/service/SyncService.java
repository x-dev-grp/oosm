package com.xdev.ooms.conditioning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.conditioning.dto.SyncRequestDto;
import com.xdev.ooms.conditioning.model.OfflineOperation;
import com.xdev.ooms.conditioning.repository.OfflineOperationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;

@Service
public class SyncService {

    private final OfflineOperationRepository logRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String internalBaseUrl;

    public SyncService(OfflineOperationRepository logRepository,
                       RestTemplate restTemplate,
                       ObjectMapper objectMapper,
                       @Value("${app.internal-base-url}") String internalBaseUrl) {
        this.logRepository = logRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.internalBaseUrl = internalBaseUrl.replaceAll("/+$", "");
    }

    @Transactional
    public void processSync(SyncRequestDto request) {
        // 1. Vérifier si l'opération a déjà été traitée
        if (logRepository.existsByOperationId(request.getOperationId())) {
            return;
        }

        // 2. Créer l'entité log
        OfflineOperation log = new OfflineOperation();
        log.setOperationId(request.getOperationId());
        log.setUrl(request.getUrl());
        log.setMethod(request.getMethod());

        // Convertir la chaîne JSON en JsonNode pour le stockage JSONB
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode requestBodyNode = objectMapper.readTree(request.getBody());
            log.setRequestBody(requestBodyNode);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON body: " + request.getBody(), e);
        }

        log.setSyncedAt(LocalDateTime.now());

        try {
            // 3. Construire l'URL complète (utilise l'URL de base configurée)
            String fullUrl = internalBaseUrl + request.getUrl();

            // 4. Récupérer le token JWT depuis le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                throw new RuntimeException("Aucun token JWT trouvé dans le contexte de sécurité");
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String token = jwt.getTokenValue();

            // 5. Préparer les en-têtes
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token); // équivalent à "Authorization: Bearer ..."

            HttpEntity<String> entity = new HttpEntity<>(request.getBody(), headers);

            // 6. Exécuter l'appel interne
            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.valueOf(request.getMethod().toUpperCase()),
                    entity,
                    String.class
            );

            // 7. Succès : convertir la réponse en JsonNode et sauvegarder
            JsonNode responseBodyNode = objectMapper.readTree(response.getBody());
            log.setResponseBody(responseBodyNode);
            log.setStatus("SYNCED");
            logRepository.save(log);

        } catch (Exception e) {
            // 8. Erreur : enregistrer l'erreur et relancer l'exception
            log.setStatus("ERROR");
            log.setErrorMessage(e.getMessage());
            logRepository.save(log);
            throw new RuntimeException("Erreur lors de la synchronisation", e);
        }
    }
}

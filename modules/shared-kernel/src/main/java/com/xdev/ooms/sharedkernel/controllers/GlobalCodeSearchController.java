package com.xdev.ooms.sharedkernel.controllers;

import com.xdev.ooms.sharedkernel.qr.model.GlobalCodeSearchResponse;
import com.xdev.ooms.sharedkernel.qr.model.QrResolveResponse;
import com.xdev.ooms.sharedkernel.services.GlobalCodeSearchContributor;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class GlobalCodeSearchController {

    private final List<GlobalCodeSearchContributor> contributors;

    public GlobalCodeSearchController(List<GlobalCodeSearchContributor> contributors) {
        this.contributors = contributors;
    }

    @GetMapping("/by-code")
    public ResponseEntity<GlobalCodeSearchResponse> searchByCode(
            @RequestParam String code
    ) {
        if (code == null || code.isBlank()) { //si  le code nul ou des espace
            return ResponseEntity.badRequest().build();
        }
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);

        List<QrResolveResponse> matches = new ArrayList<>();
        for (GlobalCodeSearchContributor contributor : contributors) {
            try {
                contributor.searchByCode(normalizedCode).ifPresent(matches::add);
            } catch (Exception ex) {
                OSMLogger.logException(this.getClass(), "Contributor failed during global code search", ex);
            }
        }
        Map<String, QrResolveResponse> uniqueMatches = new LinkedHashMap<>();
        for (QrResolveResponse item : matches) {
            String key = String.join("|",
                    item.getEntityType() == null ? "" : item.getEntityType(),
                    item.getEntityId() == null ? "" : item.getEntityId(),
                    item.getPublicCode() == null ? "" : item.getPublicCode());
            uniqueMatches.putIfAbsent(key, item);
        }
        List<QrResolveResponse> sorted = new ArrayList<>(uniqueMatches.values());
        sorted.sort(Comparator.comparing(item -> item.getEntityType() == null ? "" : item.getEntityType()));

        GlobalCodeSearchResponse response = new GlobalCodeSearchResponse();
        response.setCode(normalizedCode);
        response.setMatchCount(sorted.size());
        response.setResult(sorted.isEmpty() ? null : sorted.get(0));
        response.setResults(sorted);
        return ResponseEntity.ok(response);
    }
}

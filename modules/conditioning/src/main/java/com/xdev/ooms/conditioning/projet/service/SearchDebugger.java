package com.xdev.ooms.conditioning.projet.service;

import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.conditioning.projet.repository.ProjetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.debug.search-on-startup", havingValue = "true")
public class SearchDebugger implements CommandLineRunner {
    private final ProjetRepository projetRepository;

    public SearchDebugger(ProjetRepository projetRepository) {
        this.projetRepository = projetRepository;
    }

    @Override
    @jakarta.transaction.Transactional
    public void run(String... args) throws Exception {
        System.out.println("--- DEBUG SEARCH ---");
        Iterable<Projet> projets = projetRepository.findAll();
        for (Projet p : projets) {
            System.out.println("Projet: ID=" + p.getId() + ", Code=" + p.getCode() + ", QrHex=" + p.getQrHex() + ", Tenant=" + p.getTenantId());
        }
        System.out.println("--- END DEBUG ---");
    }
}

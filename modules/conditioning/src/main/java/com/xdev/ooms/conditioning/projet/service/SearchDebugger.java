package com.xdev.ooms.conditioning.projet.service;

import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.conditioning.projet.repository.ProjetRepository;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
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
        OOSMLogger.debug(SearchDebugger.class, "Starting project search diagnostic");
        Iterable<Projet> projets = projetRepository.findAll();
        for (Projet p : projets) {
            OOSMLogger.debug(SearchDebugger.class,
                    "Project diagnostic: id={}, code={}, qrHex={}, tenant={}",
                    p.getId(), p.getCode(), p.getQrHex(), p.getTenantId());
        }
        OOSMLogger.debug(SearchDebugger.class, "Completed project search diagnostic");
    }
}

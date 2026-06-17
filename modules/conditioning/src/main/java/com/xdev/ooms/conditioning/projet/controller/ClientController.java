package com.xdev.ooms.conditioning.projet.controller;

import com.xdev.ooms.conditioning.projet.dto.ClientDto;
import com.xdev.ooms.conditioning.projet.entity.Client;
import com.xdev.ooms.conditioning.projet.service.ClientService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ordreConditionement/clients")
public class ClientController extends BaseControllerImpl<Client, ClientDto, ClientDto> {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService, ModelMapper modelMapper) {
        super(clientService, modelMapper);
        this.clientService = clientService;
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverClient(@PathVariable UUID id) {
        try {
            clientService.desactiverClient(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "desactiverClient", e);
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerClient(@PathVariable UUID id) {
        try {
            clientService.activerClient(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "activerClient", e);
        }
    }

    @Override
    protected String getResourceName() {
        return "CLIENT";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}

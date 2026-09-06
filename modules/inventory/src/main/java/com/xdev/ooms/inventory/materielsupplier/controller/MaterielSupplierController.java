package com.xdev.ooms.inventory.materielsupplier.controller;

import com.xdev.ooms.inventory.materielsupplier.dto.MaterielSupplierDto;
import com.xdev.ooms.inventory.materielsupplier.entity.MaterielSupplier;
import com.xdev.ooms.inventory.materielsupplier.service.MaterielSupplierService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/materiel-suppliers")
public class MaterielSupplierController extends BaseControllerImpl<MaterielSupplier, MaterielSupplierDto, MaterielSupplierDto> {

    private final MaterielSupplierService materielSupplierService;

    @Autowired
    public MaterielSupplierController(MaterielSupplierService materielSupplierService, ModelMapper modelMapper) {
        super(materielSupplierService, modelMapper);
        this.materielSupplierService = materielSupplierService;
    }

    @Transactional(readOnly = true)
    @GetMapping("/actifs")
    public ResponseEntity<?> getActiveSuppliers() {
        try {
            return ResponseEntity.ok(attachPermittedActions(materielSupplierService.getActiveSuppliers()));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getActiveSuppliers", e);
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activate(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(materielSupplierService.activate(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "activate", e);
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> deactivate(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(materielSupplierService.deactivate(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "deactivate", e);
        }
    }

    @Override
    protected String getResourceName() {
        return "MATERIEL_SUPPLIER";
    }
}

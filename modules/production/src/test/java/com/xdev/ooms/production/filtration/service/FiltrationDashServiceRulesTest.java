package com.xdev.ooms.production.filtration.service;

import com.xdev.ooms.production.filtration.dto.FiltrationDashDto;
import com.xdev.ooms.production.filtration.dto.FiltrationStatus;
import com.xdev.ooms.production.filtration.entity.FiltrationOperation;
import com.xdev.ooms.production.filtration.repository.FiltrationOperationRepo;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiltrationDashServiceRulesTest {

    @Mock private BaseRepository<FiltrationOperation> repository;
    @Mock private FiltrationOperationRepo filtrationOperationRepo;

    private final UUID tenantId = UUID.randomUUID();
    private FiltrationDashService service;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(tenantId);
        service = new FiltrationDashService(repository, filtrationOperationRepo, new ModelMapper());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void completedFiltrationCannotBeDeleted() {
        FiltrationOperation op = operation(FiltrationStatus.COMPLETED);
        when(repository.findByIdAndIsDeletedFalse(op.getId())).thenReturn(Optional.of(op));

        assertThrows(IllegalStateException.class, () -> service.delete(op.getId()));
        verify(repository, never()).save(any());
    }

    @Test
    void createdFiltrationIsSoftDeleted() {
        FiltrationOperation op = operation(FiltrationStatus.CREATED);
        when(repository.findByIdAndIsDeletedFalse(op.getId())).thenReturn(Optional.of(op));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.remove(op.getId());

        assertTrue(op.getDeleted());
    }

    @Test
    void genericCreateAndUpdateAreRefused() {
        FiltrationOperation op = operation(FiltrationStatus.CREATED);
        when(repository.findByIdAndIsDeletedFalse(op.getId())).thenReturn(Optional.of(op));
        FiltrationDashDto dto = new FiltrationDashDto();
        dto.setId(op.getId());

        assertThrows(UnsupportedOperationException.class, () -> service.save(new FiltrationDashDto()));
        assertThrows(UnsupportedOperationException.class, () -> service.update(dto));
    }

    @Test
    void completedFiltrationOnlyOffersRead() {
        assertEquals(Set.of(Action.READ), service.actionsMapping(operation(FiltrationStatus.COMPLETED)));
    }

    private FiltrationOperation operation(FiltrationStatus status) {
        FiltrationOperation op = new FiltrationOperation();
        op.setId(UUID.randomUUID());
        op.setTenantId(tenantId);
        op.setStatus(status);
        return op;
    }
}

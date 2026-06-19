package com.xdev.ooms.inventory.materielsupplier.repository;

import com.xdev.ooms.inventory.materielsupplier.entity.MaterielSupplier;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterielSupplierRepository extends BaseRepository<MaterielSupplier> {

    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
    boolean existsByNumeroTva(String numeroTva);
    List<MaterielSupplier> findByActifTrue();
    List<MaterielSupplier> findByActifTrueAndIsDeletedFalse();
}

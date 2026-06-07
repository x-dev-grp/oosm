package com.xdev.ooms.inventory.repository;

import com.xdev.ooms.inventory.entity.Fournisseur;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FournisseurRepository extends BaseRepository<Fournisseur> {

    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
    boolean existsByNumeroTva(String numeroTva);
    List<Fournisseur> findByActifTrue();

}
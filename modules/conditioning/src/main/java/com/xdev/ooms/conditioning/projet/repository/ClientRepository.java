package com.xdev.ooms.conditioning.projet.repository;



import com.xdev.ooms.conditioning.projet.entity.Client;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends BaseRepository<Client> {

    boolean existsByCodeClient(String codeClient);

    boolean existsByEmail(String email);
    boolean existsByNumeroTva(String numeroTva);
    boolean existsBySiret(String siret);
    boolean existsByTelephone(String telephone);
}
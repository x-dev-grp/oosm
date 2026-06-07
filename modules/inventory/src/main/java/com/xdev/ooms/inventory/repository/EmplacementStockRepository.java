package com.xdev.ooms.inventory.repository;

import com.xdev.ooms.inventory.Enum.TypeEmplacement;
import com.xdev.ooms.inventory.entity.EmplacementStock;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmplacementStockRepository extends BaseRepository<EmplacementStock> {

    Optional<EmplacementStock> findByCode(String code);

    List<EmplacementStock> findByTypeEmplacement(TypeEmplacement type);

    List<EmplacementStock> findByZone(String zone);

    List<EmplacementStock> findByDisponibleTrue();

    @Query("SELECT e FROM EmplacementStock e WHERE e.zone = :zone AND e.disponible = true")
    List<EmplacementStock> findDisponiblesParZone(@Param("zone") String zone);

    @Query("SELECT e FROM EmplacementStock e WHERE e.reservePour = :client")
    List<EmplacementStock> findReservesPour(@Param("client") String client);


    @Query("SELECT e FROM EmplacementStock e WHERE e.temperatureMin IS NOT NULL OR e.temperatureMax IS NOT NULL")
    List<EmplacementStock> findEmplacementsTemperatureControlee();

    boolean existsByCode(String code);


}
package com.xdev.ooms.production.equipment.repository;

import com.xdev.ooms.production.equipment.entity.EquipmentServiceMission;
import com.xdev.ooms.production.equipment.enums.EquipmentServiceMissionStatus;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface EquipmentServiceMissionRepository extends BaseRepository<EquipmentServiceMission> {

    boolean existsByEquipment_IdAndStatusIn(
            UUID equipmentId,
            Collection<EquipmentServiceMissionStatus> statuses);

    boolean existsByEquipment_IdAndStatusInAndIdNot(
            UUID equipmentId,
            Collection<EquipmentServiceMissionStatus> statuses,
            UUID excludeId);
}

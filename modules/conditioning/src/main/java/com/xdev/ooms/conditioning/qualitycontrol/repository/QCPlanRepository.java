package com.xdev.ooms.conditioning.qualitycontrol.repository;


import com.xdev.ooms.conditioning.qualitycontrol.entity.QCPlan;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import java.util.Optional;
import java.util.UUID;

public interface QCPlanRepository extends BaseRepository<QCPlan> {
    Optional<QCPlan> findByOfIdAndActifTrue(UUID ofId);
}
package com.xdev.ooms.production.machineplan.repository;



import com.xdev.ooms.production.machineplan.entity.MachinePlan;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MachinePlanRepository extends BaseRepository<MachinePlan> {
    // Add custom queries if needed.
}

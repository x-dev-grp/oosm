package com.xdev.ooms.conditioning.repository;


import com.xdev.ooms.conditioning.model.QCControlPoint;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;

import java.util.List;
import java.util.UUID;

public interface QCControlPointRepository extends BaseRepository<QCControlPoint> {
        List<QCControlPoint> findByPlanIdAndBlockingTrue(UUID planId);
        List<QCControlPoint> findByPlanId(UUID planId);
    }

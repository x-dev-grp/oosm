package com.xdev.ooms.hr.pointage.component;

import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.hr.pointage.repository.PointageRepository;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Basic attendance anomaly heuristics for pointage records.
 */
@Component
public class AttendanceAnomalyDetector {

    private static final LocalTime DEFAULT_LATE_THRESHOLD = LocalTime.of(8, 30);
    private static final LocalTime DEFAULT_EARLY_DEPARTURE = LocalTime.of(16, 0);

    private final PointageRepository pointageRepository;

    public AttendanceAnomalyDetector(PointageRepository pointageRepository) {
        this.pointageRepository = pointageRepository;
    }

    public List<String> detect(Pointage pointage) {
        List<String> codes = new ArrayList<>();
        if (pointage == null) {
            return codes;
        }

        if (pointage.getCheckIn() == null) {
            codes.add("MISSING_CHECK_IN");
        }
        if (pointage.getCheckOut() == null) {
            codes.add("MISSING_CHECK_OUT");
        }

        if (pointage.getLateMinutes() != null && pointage.getLateMinutes() > 0) {
            codes.add("LATE_ARRIVAL");
        } else if (pointage.getCheckIn() != null && pointage.getCheckIn().isAfter(DEFAULT_LATE_THRESHOLD)) {
            codes.add("LATE_ARRIVAL");
        }

        if (pointage.getCheckOut() != null && pointage.getCheckOut().isBefore(DEFAULT_EARLY_DEPARTURE)) {
            codes.add("EARLY_DEPARTURE");
        }

        if (isDuplicate(pointage)) {
            codes.add("DUPLICATE_ENTRY");
        }

        return codes;
    }

    public String detectAsCsv(Pointage pointage) {
        List<String> codes = detect(pointage);
        if (codes.isEmpty()) {
            return null;
        }
        return String.join(",", codes);
    }

    private boolean isDuplicate(Pointage pointage) {
        if (pointage.getEmployee() == null || pointage.getEmployee().getId() == null || pointage.getWorkDate() == null) {
            return false;
        }
        UUID excludeId = pointage.getId();
        return pointageRepository.existsByEmployee_IdAndWorkDateAndIdNotAndIsDeletedFalse(
                pointage.getEmployee().getId(),
                pointage.getWorkDate(),
                excludeId
        );
    }
}

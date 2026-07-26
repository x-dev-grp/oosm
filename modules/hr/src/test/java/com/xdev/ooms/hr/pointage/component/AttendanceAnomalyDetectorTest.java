package com.xdev.ooms.hr.pointage.component;

import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.hr.pointage.repository.PointageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttendanceAnomalyDetectorTest {

    private PointageRepository pointageRepository;
    private AttendanceAnomalyDetector detector;

    @BeforeEach
    void setUp() {
        pointageRepository = mock(PointageRepository.class);
        detector = new AttendanceAnomalyDetector(pointageRepository);
        when(pointageRepository.existsByEmployee_IdAndWorkDateAndIdNotAndIsDeletedFalse(
                any(), any(), any())).thenReturn(false);
        when(pointageRepository.existsByEmployee_IdAndWorkDateAndIdNotAndIsDeletedFalse(
                any(), any(), isNull())).thenReturn(false);
    }

    @Test
    void nullPointage_returnsEmpty() {
        assertTrue(detector.detect(null).isEmpty());
        assertNull(detector.detectAsCsv(null));
    }

    @Test
    void missingCheckInAndCheckOut_flagsBoth() {
        Pointage pointage = basePointage();
        pointage.setCheckIn(null);
        pointage.setCheckOut(null);

        List<String> codes = detector.detect(pointage);

        assertTrue(codes.contains("MISSING_CHECK_IN"));
        assertTrue(codes.contains("MISSING_CHECK_OUT"));
    }

    @Test
    void lateArrivalByThreshold_flagsLateArrival() {
        Pointage pointage = basePointage();
        pointage.setCheckIn(LocalTime.of(9, 0));
        pointage.setCheckOut(LocalTime.of(17, 0));
        pointage.setLateMinutes(null);

        List<String> codes = detector.detect(pointage);

        assertTrue(codes.contains("LATE_ARRIVAL"));
        assertFalse(codes.contains("EARLY_DEPARTURE"));
    }

    @Test
    void earlyDeparture_flagsEarlyDeparture() {
        Pointage pointage = basePointage();
        pointage.setCheckIn(LocalTime.of(8, 0));
        pointage.setCheckOut(LocalTime.of(15, 0));

        List<String> codes = detector.detect(pointage);

        assertTrue(codes.contains("EARLY_DEPARTURE"));
        assertFalse(codes.contains("LATE_ARRIVAL"));
    }

    @Test
    void duplicateEntry_flagsDuplicate() {
        UUID employeeId = UUID.randomUUID();
        UUID pointageId = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(employeeId);

        Pointage pointage = basePointage();
        pointage.setId(pointageId);
        pointage.setEmployee(employee);
        pointage.setWorkDate(LocalDate.of(2026, 3, 10));
        pointage.setCheckIn(LocalTime.of(8, 0));
        pointage.setCheckOut(LocalTime.of(17, 0));

        when(pointageRepository.existsByEmployee_IdAndWorkDateAndIdNotAndIsDeletedFalse(
                eq(employeeId), eq(LocalDate.of(2026, 3, 10)), eq(pointageId)))
                .thenReturn(true);

        List<String> codes = detector.detect(pointage);

        assertTrue(codes.contains("DUPLICATE_ENTRY"));
        assertEquals("DUPLICATE_ENTRY", detector.detectAsCsv(pointage));
    }

    @Test
    void cleanAttendance_returnsNoCodes() {
        Pointage pointage = basePointage();
        pointage.setCheckIn(LocalTime.of(8, 0));
        pointage.setCheckOut(LocalTime.of(17, 0));
        pointage.setLateMinutes(0);

        List<String> codes = detector.detect(pointage);

        assertTrue(codes.isEmpty());
        assertNull(detector.detectAsCsv(pointage));
    }

    private static Pointage basePointage() {
        Pointage pointage = new Pointage();
        pointage.setWorkDate(LocalDate.of(2026, 3, 10));
        return pointage;
    }
}

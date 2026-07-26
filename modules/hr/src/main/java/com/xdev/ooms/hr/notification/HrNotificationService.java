package com.xdev.ooms.hr.notification;

import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Thin helper around {@link NotificationPort} for HR domain events.
 */
@Service
public class HrNotificationService {

    private static final Logger log = LoggerFactory.getLogger(HrNotificationService.class);

    public static final String LEAVE_REQUEST_APPROVED = "LEAVE_REQUEST_APPROVED";

    private final NotificationPort notificationPort;

    public HrNotificationService(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    public void notifyLeaveApproved(LeaveRequest leave, UUID actorUserId, String actorDisplayName) {
        if (leave == null || leave.getId() == null) {
            return;
        }
        try {
            Map<String, String> recap = new LinkedHashMap<>();
            if (leave.getEmployee() != null) {
                recap.put("employee",
                        nullSafe(leave.getEmployee().getFirstName()) + " " + nullSafe(leave.getEmployee().getLastName()));
            }
            if (leave.getLeaveType() != null) {
                recap.put("leaveType", leave.getLeaveType().name());
            }
            if (leave.getStartDate() != null) {
                recap.put("startDate", leave.getStartDate().toString());
            }
            if (leave.getEndDate() != null) {
                recap.put("endDate", leave.getEndDate().toString());
            }
            String label = leave.getEmployee() != null
                    ? nullSafe(leave.getEmployee().getFirstName()) + " " + nullSafe(leave.getEmployee().getLastName())
                    : leave.getId().toString();
            notificationPort.publish(new NotificationEvent(
                    LEAVE_REQUEST_APPROVED,
                    leave.getId(),
                    label.trim(),
                    recap,
                    actorUserId,
                    actorDisplayName
            ));
        } catch (RuntimeException ex) {
            log.warn("Failed to publish leave-approved notification for {}: {}", leave.getId(), ex.getMessage());
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}

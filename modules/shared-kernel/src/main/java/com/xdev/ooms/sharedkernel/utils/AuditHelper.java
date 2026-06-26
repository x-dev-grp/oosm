package com.xdev.ooms.sharedkernel.utils;

 import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AuditHelper {

    private static Optional<String> currentUserId() {
        try {
            Optional<Map<String, Object>> oosmUserOpt = SecurityUtils.getCurrentOsmUser();
            if (oosmUserOpt.isEmpty()) return Optional.empty();
            Map<String, Object> oosmUser = oosmUserOpt.get();
            
            String roleStr = "";
            Object roleObj = oosmUser.get("role");
            if (roleObj instanceof Map<?, ?>) {
                Map<String, Object> roleMap = (Map<String, Object>) roleObj;
                Object roleName = roleMap.get("roleName");
                if (roleName != null) {
                    roleStr = " (" + roleName + ")";
                }
            }

            Object firstName = oosmUser.get("firstName");
            Object lastName = oosmUser.get("lastName");
            if (firstName != null || lastName != null) {
                String fName = firstName != null ? String.valueOf(firstName) : "";
                String lName = lastName != null ? String.valueOf(lastName) : "";
                String fullName = (fName + " " + lName).trim() + roleStr;
                if (!fullName.trim().isEmpty()) return Optional.of(fullName.trim());
            }

            Object username = oosmUser.get("username");
            if (username != null) return Optional.of(String.valueOf(username) + roleStr);

            Object id = oosmUser.get("id");
            if (id != null) return Optional.of(String.valueOf(id) + roleStr);
            Object externalId = oosmUser.get("externalId");
            if (externalId != null) return Optional.of(String.valueOf(externalId) + roleStr);
        } catch (Exception ignored) {}
        return Optional.empty();
    }


    private static Object convertValueForField(Field field, Object value) {
        if (value == null) return null;
        Class<?> type = field.getType();
        if (type == UUID.class && !(value instanceof UUID)) {
            try { return UUID.fromString(String.valueOf(value)); } catch (Exception ignored) {}
        }
        if (type == Instant.class && value instanceof LocalDateTime ldt) {
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        }
        if (type == LocalDateTime.class && value instanceof Instant inst) {
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
        }
        return value;
    }
    private AuditHelper() {}

    public static <E> void applyAuditOnCreate(E entity) {
        currentUserId().ifPresent(uid -> {
            setFieldIfNull(entity, "createdBy", uid);
            setField(entity, "lastModifiedBy", uid);
        });

        setFieldIfNull(entity, "createdDate", Instant.now());
        setField(entity, "lastModifiedDate", Instant.now());
        setFieldIfNull(entity, "tenantId", TenantContext.getCurrentTenant());
    }

    public static <E> void applyAuditOnUpdate(E entity) {
        currentUserId().ifPresent(uid -> setField(entity, "lastModifiedBy", uid));
        setField(entity, "lastModifiedDate", Instant.now());
    }
    private static void setFieldIfNull(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null) return;
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        if (field == null) return;
        ReflectionUtils.makeAccessible(field);
        try {
            Object current = field.get(target);
            if (current == null) {
                ReflectionUtils.setField(field, target, convertValueForField(field, value));
            }
        } catch (IllegalAccessException ignored) {}
    }

    private static void setField(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null) return;
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        if (field == null) return;
        ReflectionUtils.makeAccessible(field);
        try {
            ReflectionUtils.setField(field, target, convertValueForField(field, value));
        } catch (IllegalArgumentException ignored) {}
    }
}

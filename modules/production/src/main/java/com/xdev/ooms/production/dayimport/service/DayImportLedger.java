package com.xdev.ooms.production.dayimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xdev.ooms.production.dayimport.dto.DayImportReportDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** All business identity writes must join the caller's business transaction. */
@Service
public class DayImportLedger {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public DayImportLedger(JdbcTemplate jdbc, ObjectMapper mapper) { this.jdbc = jdbc; this.mapper = mapper; }

    public static String digest(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    public String payload(Object row, Object... context) {
        ObjectNode value = mapper.valueToTree(row);
        value.set("context", mapper.valueToTree(context));
        clean(value);
        return digest(value.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void clean(com.fasterxml.jackson.databind.JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.remove("rowNumber");
            java.util.List<String> names = new java.util.ArrayList<>(); object.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                var child=object.get(name);
                if (name.toLowerCase(Locale.ROOT).endsWith("ref") && child.isTextual()) object.put(name, key(child.asText()));
                else clean(child);
            }
        } else if (node.isArray()) { node.forEach(this::clean); }
    }

    public String operation(String kind, String ref) {
        return jdbc.query("SELECT payload_digest FROM day_import_operation WHERE tenant_id=? AND operation_kind=? AND external_ref=?",
                (rs, n) -> rs.getString(1), DayImportAccess.tenant(), kind, key(ref)).stream().findFirst().orElse(null);
    }

    public void record(String kind, String ref, String payload, UUID entityId) {
        jdbc.update("INSERT INTO day_import_operation(tenant_id,operation_kind,external_ref,payload_digest,entity_id) VALUES (?,?,?,?,?)",
                DayImportAccess.tenant(), kind, key(ref), payload, entityId);
    }

    public UUID entity(String kind, String ref) {
        return jdbc.query("SELECT entity_id FROM day_import_operation WHERE tenant_id=? AND operation_kind=? AND external_ref=?",
                (rs,n) -> rs.getObject(1, UUID.class), DayImportAccess.tenant(), kind, key(ref)).stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    public boolean knownEntity(String kind, UUID entityId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM day_import_operation WHERE tenant_id=? AND operation_kind=? AND entity_id=?)",
                Boolean.class, DayImportAccess.tenant(), kind, entityId));
    }

    public static String key(String ref) { return ref == null ? "" : ref.trim().toLowerCase(Locale.ROOT); }

    public void lockTenant() {
        // Transaction-scoped database lock serializes manual/Drive imports across application instances.
        jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", rs -> { }, "day-import:" + DayImportAccess.tenant());
    }

    public void createRun(UUID id, String fileDigest, String source, String actor, DayImportReportDto report) {
        jdbc.update("INSERT INTO day_import_run(id,tenant_id,file_digest,source,actor,outcome,report) VALUES (?,?,?,?,?,'PREVIEW',?)",
                id, DayImportAccess.tenant(), fileDigest, source, actor, json(report));
    }

    public Run run(UUID id) {
        return jdbc.query("SELECT id,file_digest,outcome,report,created_at FROM day_import_run WHERE id=? AND tenant_id=?",
                (rs,n) -> new Run(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), read(rs.getString(4)), rs.getTimestamp(5).toInstant()),
                id, DayImportAccess.tenant()).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Import run not found"));
    }

    public DayImportReportDto committed(String digest) {
        return jdbc.query("SELECT report FROM day_import_run WHERE tenant_id=? AND file_digest=? AND outcome='COMMITTED'",
                (rs,n) -> read(rs.getString(1)), DayImportAccess.tenant(), digest).stream().findFirst().orElse(null);
    }

    public void finish(UUID id, String outcome, DayImportReportDto report) {
        jdbc.update("UPDATE day_import_run SET outcome=?,report=?,completed_at=CURRENT_TIMESTAMP WHERE id=? AND tenant_id=? AND outcome='PREVIEW'",
                outcome, json(report), id, DayImportAccess.tenant());
    }

    public String effects(DayImportReportDto report) {
        ObjectNode value = mapper.valueToTree(report);
        value.remove(List.of("runId", "outcome"));
        return value.toString();
    }

    public String json(Object value) {
        try { return mapper.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); }
    }
    private DayImportReportDto read(String value) {
        try { return mapper.readValue(value, DayImportReportDto.class); } catch (Exception e) { throw new IllegalStateException(e); }
    }
    public record Run(UUID id, String digest, String outcome, DayImportReportDto report, java.time.Instant createdAt) {}
}

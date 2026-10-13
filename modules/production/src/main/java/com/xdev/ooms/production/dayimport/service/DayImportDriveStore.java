package com.xdev.ooms.production.dayimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.production.dayimport.dto.DayImportDriveStatusDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DayImportDriveStore {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public DayImportDriveStore(JdbcTemplate jdbc, ObjectMapper mapper) { this.jdbc=jdbc; this.mapper=mapper; }
    public DayImportDriveStatusDto status() {
        return jdbc.query("SELECT status FROM day_import_drive_state WHERE tenant_id=?", (rs,n) -> {
            try { return mapper.readValue(rs.getString(1), DayImportDriveStatusDto.class); }
            catch (Exception e) { throw new IllegalStateException(e); }
        }, DayImportAccess.tenant()).stream().findFirst().orElseGet(DayImportDriveStatusDto::new);
    }
    public UUID acquire() {
        UUID lease=UUID.randomUUID();
        int changed=jdbc.update("""
            INSERT INTO day_import_drive_state(tenant_id,lease_id,lease_until) VALUES (?,?,CURRENT_TIMESTAMP + INTERVAL '5 minutes')
            ON CONFLICT(tenant_id) DO UPDATE SET lease_id=excluded.lease_id,lease_until=excluded.lease_until
            WHERE day_import_drive_state.lease_until IS NULL OR day_import_drive_state.lease_until < CURRENT_TIMESTAMP
            """, DayImportAccess.tenant(), lease);
        return changed == 1 ? lease : null;
    }
    public void renew(UUID lease) {
        if (jdbc.update("UPDATE day_import_drive_state SET lease_until=CURRENT_TIMESTAMP + INTERVAL '5 minutes' WHERE tenant_id=? AND lease_id=? AND lease_until>CURRENT_TIMESTAMP",
                DayImportAccess.tenant(), lease) != 1) throw new IllegalStateException("Drive sync lease expired");
    }
    public void finish(UUID lease, DayImportDriveStatusDto status) {
        try { jdbc.update("UPDATE day_import_drive_state SET status=?,lease_id=NULL,lease_until=NULL WHERE tenant_id=? AND lease_id=?",
                mapper.writeValueAsString(status), DayImportAccess.tenant(), lease); }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException(e); }
    }
    public void routing(String fileId, String digest, String folder) {
        jdbc.update("""
            INSERT INTO day_import_drive_file(tenant_id,file_id,file_digest,target_folder,routed) VALUES (?,?,?,?,FALSE)
            ON CONFLICT(tenant_id,file_id) DO UPDATE SET file_digest=excluded.file_digest,target_folder=excluded.target_folder,routed=FALSE
            """, DayImportAccess.tenant(), fileId, digest, folder);
    }
    public void routed(String fileId) {
        jdbc.update("UPDATE day_import_drive_file SET routed=TRUE WHERE tenant_id=? AND file_id=?", DayImportAccess.tenant(), fileId);
    }
    public List<Pending> pending() {
        return jdbc.query("SELECT file_id,file_digest,target_folder FROM day_import_drive_file WHERE tenant_id=? AND routed=FALSE",
                (rs,n)->new Pending(rs.getString(1),rs.getString(2),rs.getString(3)), DayImportAccess.tenant());
    }
    public record Pending(String fileId,String digest,String folder) {}
}

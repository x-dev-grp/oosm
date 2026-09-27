package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.controller.DayImportController;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DayImportControllerTest {
    MockMvc mvc;
    DayImportWorkflow workflow;
    DayImportDriveService drive;
    @BeforeEach void setup() {
        workflow=mock(DayImportWorkflow.class);drive=mock(DayImportDriveService.class);
        mvc=MockMvcBuilders.standaloneSetup(new DayImportController(mock(DayImportService.class),workflow,mock(DayImportReportExporter.class),drive)).build();
        TenantContext.setCurrentTenant(UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("operator","",List.of(new SimpleGrantedAuthority("RECEPTION:UNIFIEDDELIVERY:READ"))));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext();TenantContext.clear(); }
    @Test void directCommitWithoutCreatePermissionReturnsForbiddenBeforeProcessing() throws Exception {
        mvc.perform(multipart("/api/production/import/day/commit").file(new MockMultipartFile("file","test.xlsx","application/octet-stream",new byte[]{1})).param("previewId",UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());verifyNoInteractions(workflow);
    }
    @Test void operatorCannotDisconnectOrTriggerDriveSync() throws Exception {
        mvc.perform(post("/api/production/import/day/drive/oauth/disconnect")).andExpect(status().isForbidden());
        mvc.perform(post("/api/production/import/day/drive/sync")).andExpect(status().isForbidden());verifyNoInteractions(drive);
    }
}

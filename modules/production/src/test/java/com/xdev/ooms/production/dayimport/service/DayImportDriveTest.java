package com.xdev.ooms.production.dayimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.production.dayimport.dto.*;
import com.xdev.ooms.production.dayimport.repository.TenantGoogleDriveCredentialRepository;
import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.production.parameter.service.*;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.net.*;
import java.net.http.HttpClient;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DayImportDriveTest {
    DayImportDriveClient client;
    DayImportWorkflow workflow;
    DayImportDriveStore store;
    GoogleDriveOAuthService oauth;
    DayImportDriveService service;
    @BeforeEach void setup() {
        TenantContext.setCurrentTenant(UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("review","",List.of(new SimpleGrantedAuthority("ADMIN"))));
        client=mock(DayImportDriveClient.class);workflow=mock(DayImportWorkflow.class);store=mock(DayImportDriveStore.class);oauth=mock(GoogleDriveOAuthService.class);
        var enabled=mock(BooleanParameterReader.class);var parameters=mock(ParameterService.class);
        when(enabled.isEnabled("IMPORT_GDRIVE_ENABLED",false)).thenReturn(true);
        when(oauth.isOAuthConfigured()).thenReturn(true);when(oauth.isConnected(any())).thenReturn(true);
        when(parameters.getByCode(anyString(),any())).thenAnswer(i->{var p=new Parameter();p.setValue(i.getArgument(0).equals("IMPORT_GDRIVE_FOLDER_ID")?"source":"processed");return p;});
        when(store.status()).thenAnswer(i->new DayImportDriveStatusDto());when(store.acquire()).thenReturn(UUID.randomUUID());
        service=new DayImportDriveService(enabled,parameters,client,workflow,store,oauth,mock(TenantGoogleDriveCredentialRepository.class));
    }
    @AfterEach void clear() { TenantContext.clear();SecurityContextHolder.clearContext(); }
    @Test void routingRetryDoesNotReplayBusinessImport() throws Exception {
        byte[] bytes={1,2,3};when(store.pending()).thenReturn(List.of(new DayImportDriveStore.Pending("file",DayImportLedger.digest(bytes),"processed")));
        when(client.download("file")).thenReturn(bytes);
        var result=service.syncNow();assertEquals("OK",result.getLastResult());verify(store).routed("file");verifyNoInteractions(workflow);
    }
    @Test void failedMoveRemainsCommittedAndPending() throws Exception {
        byte[] bytes={1};when(client.listXlsx("source")).thenReturn(List.of(new DayImportDriveClient.DriveFileRef("file","day.xlsx")));when(client.download("file")).thenReturn(bytes);
        var report=new DayImportReportDto();report.setRunId(UUID.randomUUID());report.setCanCommit(true);report.setOutcome("COMMITTED");
        when(workflow.preview(bytes,"DRIVE")).thenReturn(report);when(workflow.commit(bytes,report.getRunId())).thenReturn(report);
        doThrow(new IllegalStateException("move failed")).when(client).moveToFolder("file","processed");
        var result=service.syncNow();assertEquals("COMMITTED_ROUTING_PENDING",result.getLastResult());assertEquals(1,result.getProcessedCount());assertEquals(0,result.getFailedCount());
        verify(store).routing("file",DayImportLedger.digest(bytes),"processed");verify(store,never()).routed(anyString());
    }
    @Test void changedPendingFileIsNotMovedOrImported() throws Exception {
        when(store.pending()).thenReturn(List.of(new DayImportDriveStore.Pending("file","old-digest","processed")));when(client.download("file")).thenReturn(new byte[]{9});
        assertEquals("COMMITTED_ROUTING_PENDING",service.syncNow().getLastResult());verifyNoInteractions(workflow);verify(client,never()).moveToFolder(anyString(),anyString());
    }
    @Test void clientReadsAllPagesBeyondOneHundredFiles() throws Exception {
        var server=com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        var requests=new java.util.concurrent.atomic.AtomicInteger();
        server.createContext("/files", exchange->{
            requests.incrementAndGet();boolean next=exchange.getRequestURI().getQuery().contains("pageToken=next");
            StringBuilder body=new StringBuilder("{\"files\":[");int count=next?1:100;
            for(int i=0;i<count;i++){if(i>0)body.append(',');body.append("{\"id\":\"").append(next?100:i).append("\",\"name\":\"day.xlsx\"}");}
            body.append(next?"]}":"],\"nextPageToken\":\"next\"}");byte[] bytes=body.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,bytes.length);try(var out=exchange.getResponseBody()){out.write(bytes);}
        });
        server.start();
        try {
            when(oauth.getAccessTokenForCurrentTenant()).thenReturn("local-test-token");
            var client=new TenantGoogleDriveClient(oauth,new ObjectMapper(),HttpClient.newHttpClient(),"http://127.0.0.1:"+server.getAddress().getPort());
            assertEquals(101,client.listXlsx("source").size());assertEquals(2,requests.get());
        } finally {server.stop(0);}
    }
}

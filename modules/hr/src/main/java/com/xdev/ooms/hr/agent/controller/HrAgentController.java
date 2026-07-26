package com.xdev.ooms.hr.agent.controller;

import com.xdev.ooms.hr.agent.dto.AgentQueryRequest;
import com.xdev.ooms.hr.agent.dto.AgentResponse;
import com.xdev.ooms.hr.agent.service.HrAgentService;
import com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/agent")
public class HrAgentController {

    private final HrAgentService hrAgentService;

    public HrAgentController(HrAgentService hrAgentService) {
        this.hrAgentService = hrAgentService;
    }

    @PostMapping("/query")
    public ResponseEntity<ApiResponse<AgentResponse>> query(@RequestBody AgentQueryRequest request) {
        try {
            AgentResponse response = hrAgentService.query(request);
            return ResponseEntity.ok(new ApiResponse<>(response.isSuccess(), response.getMessage(), response));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

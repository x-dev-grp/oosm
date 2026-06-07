package com.xdev.ooms.production.controller;


import com.xdev.ooms.production.dto.*;
import com.xdev.ooms.production.dto.FiltrationAnalyticsDto;
import com.xdev.ooms.production.service.ProdAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/production/analytics")
public class AnalyticsController {



    private final ProdAnalyticsService prodAnalyticsService;

    public AnalyticsController(ProdAnalyticsService prodAnalyticsService) {
        this.prodAnalyticsService = prodAnalyticsService;
    }


    // 23.5 Efficacite Filtrage - expose données reelles à osm-cond via Feign
    @GetMapping("/filtration")

    public ResponseEntity<List<FiltrationAnalyticsDto>> getFiltrationReport(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(prodAnalyticsService.getFiltrationReport(startDate, endDate));
    }
}

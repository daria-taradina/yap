package com.yap.backend.controllers;

import com.yap.backend.dtos.ReportRequest;
import com.yap.backend.entities.Report;
import com.yap.backend.services.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReport(@RequestBody @Valid ReportRequest request) {
        Report report = reportService.createReport(request);
        return ResponseEntity.status(201).body(Map.of(
                "reportId", report.getReportId(),
                "status", report.getStatus().name()
        ));
    }
}

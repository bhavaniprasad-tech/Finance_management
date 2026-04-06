package com.bhavaniprasad.moneymanager.controller;

import com.bhavaniprasad.moneymanager.dto.DashboardSummaryDTO;
import com.bhavaniprasad.moneymanager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardSummaryDTO> getDashboardData() {
        DashboardSummaryDTO dashboardData = dashboardService.getDashboard();
        return ResponseEntity.ok(dashboardData);
    }



}

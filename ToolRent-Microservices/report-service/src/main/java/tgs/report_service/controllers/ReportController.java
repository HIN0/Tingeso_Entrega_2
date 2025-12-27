package tgs.report_service.controllers;

import tgs.report_service.models.ClientDTO;
import tgs.report_service.models.LoanDTO;
import tgs.report_service.models.ReportSummaryDTO;
import tgs.report_service.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDTO> getSummary() {
        return ResponseEntity.ok(reportService.generateGeneralReport());
    }

    @GetMapping("/active-loans")
    public ResponseEntity<List<LoanDTO>> getActiveLoans() {
        return ResponseEntity.ok(reportService.getActiveLoans());
    }

    @GetMapping("/delinquent-clients")
    public ResponseEntity<List<ClientDTO>> getDelinquentClients() {
        return ResponseEntity.ok(reportService.getDelinquentClients());
    }

    @GetMapping("/tool-ranking")
    public ResponseEntity<List<Map<String, Object>>> getToolRanking() {
        return ResponseEntity.ok(reportService.getToolRanking());
    }
}
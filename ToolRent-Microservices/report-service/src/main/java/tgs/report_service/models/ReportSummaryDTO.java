package tgs.report_service.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportSummaryDTO {
    private int totalLoans;
    private int activeLoans;
    private int returnedLoans;
    private double totalEarnings; // Ganancia total estimada
    private double totalLateFees; // Multas acumuladas
}
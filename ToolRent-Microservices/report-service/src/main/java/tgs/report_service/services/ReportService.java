package tgs.report_service.services;

import tgs.report_service.models.LoanDTO;
import tgs.report_service.models.ReportSummaryDTO;
import tgs.report_service.models.TariffDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private RestTemplate restTemplate;

    public ReportSummaryDTO generateGeneralReport() {
        // 1. Obtener todos los préstamos desde M2
        LoanDTO[] loansArray = restTemplate.getForObject("http://loan-service/api/loans", LoanDTO[].class);
        List<LoanDTO> loans = Arrays.asList(loansArray != null ? loansArray : new LoanDTO[0]);

        // 2. Obtener tarifas actuales desde M4
        TariffDTO tariff = restTemplate.getForObject("http://tariff-service/api/tariffs", TariffDTO.class);
        if (tariff == null) {
            tariff = new TariffDTO(); // Evitar null pointer, usar valores 0 por defecto
            tariff.setDailyRentFee(0);
            tariff.setDailyLateFee(0);
        }

        // 3. Calcular métricas
        int active = 0;
        int returned = 0;
        double earnings = 0;
        double lateFees = 0;

        for (LoanDTO loan : loans) {
            if ("ACTIVE".equals(loan.getStatus())) {
                active++;
            } else if ("RETURNED".equals(loan.getStatus())) {
                returned++;
            }

            // Cálculo simplificado de ganancias
            // Días de arriendo * tarifa diaria
            long days = ChronoUnit.DAYS.between(loan.getLoanDate(), loan.getDeadlineDate());
            if (days < 1) days = 1; // Mínimo 1 día
            earnings += days * tariff.getDailyRentFee();

            // Cálculo de multas (si aplica)
            if (loan.getReturnDate() != null && loan.getReturnDate().isAfter(loan.getDeadlineDate())) {
                long daysLate = ChronoUnit.DAYS.between(loan.getDeadlineDate(), loan.getReturnDate());
                lateFees += daysLate * tariff.getDailyLateFee();
            }
        }

        return ReportSummaryDTO.builder()
                .totalLoans(loans.size())
                .activeLoans(active)
                .returnedLoans(returned)
                .totalEarnings(earnings)
                .totalLateFees(lateFees)
                .build();
    }
}
package tgs.report_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tgs.report_service.models.*;

// --- ESTOS IMPORTS ERAN LOS QUE FALTABAN ---
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
// -------------------------------------------

@Service
public class ReportService {

    @Autowired
    private RestTemplate restTemplate;

    // Nombres de servicio tal cual aparecen en Eureka
    private final String LOAN_URL = "http://loan-service/api/loans";
    private final String CUSTOMER_URL = "http://customer-service/api/clients";
    
    // 1. Reporte de Préstamos Activos
    public List<LoanDTO> getActiveLoans() {
        // Obtenemos un Array [] porque RestTemplate no maneja bien List<?> genéricas directamente
        LoanDTO[] loans = restTemplate.getForObject(LOAN_URL, LoanDTO[].class);
        if (loans == null) return new ArrayList<>();
        
        return Arrays.stream(loans)
                .filter(l -> "ACTIVE".equals(l.getStatus()))
                .collect(Collectors.toList());
    }

    // 2. Reporte de Clientes con Atrasos/Deudas
    public List<ClientDTO> getDelinquentClients() {
        ClientDTO[] clients = restTemplate.getForObject(CUSTOMER_URL, ClientDTO[].class);
        if (clients == null) return new ArrayList<>();

        // Filtramos clientes que tengan deuda (balance > 0)
        return Arrays.stream(clients)
                .filter(c -> c.getBalance() != null && c.getBalance() > 0)
                .collect(Collectors.toList());
    }

    // 3. Ranking de Herramientas
    public List<Map<String, Object>> getToolRanking() {
        LoanDTO[] loans = restTemplate.getForObject(LOAN_URL, LoanDTO[].class);
        if (loans == null) return new ArrayList<>();

        // Agrupar por toolId y contar
        Map<Long, Long> frequencyMap = Arrays.stream(loans)
                .collect(Collectors.groupingBy(LoanDTO::getToolId, Collectors.counting()));

        List<Map<String, Object>> ranking = new ArrayList<>();
        
        for (Map.Entry<Long, Long> entry : frequencyMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("toolId", entry.getKey());
            item.put("loansCount", entry.getValue());
            ranking.add(item);
        }

        // Ordenar de mayor a menor
        ranking.sort((a, b) -> ((Long) b.get("loansCount")).compareTo((Long) a.get("loansCount")));
        
        return ranking;
    }

    public ReportSummaryDTO generateGeneralReport() {
        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.setActiveLoansCount(getActiveLoans().size());
        summary.setDelinquentClientsCount(getDelinquentClients().size());
        return summary;
    }
}
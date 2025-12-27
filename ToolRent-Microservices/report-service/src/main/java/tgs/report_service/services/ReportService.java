package tgs.report_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tgs.report_service.models.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private RestTemplate restTemplate;

    private final String LOAN_URL = "http://loan-service/api/loans";
    private final String CUSTOMER_URL = "http://customer-service/api/clients";
    
    // CORRECCIÓN: Usar endpoint filtrado si existe, o manejar nulls seguramente
    public List<LoanDTO> getActiveLoans() {
        // Idealmente: GET /api/loans/search?status=ACTIVE
        // Por ahora, mantendremos la lógica pero con validación de nulos robusta
        LoanDTO[] loans = restTemplate.getForObject(LOAN_URL, LoanDTO[].class);
        if (loans == null) return new ArrayList<>();
        
        return Arrays.stream(loans)
                .filter(l -> "ACTIVE".equalsIgnoreCase(l.getStatus()))
                .collect(Collectors.toList());
    }

    public List<ClientDTO> getDelinquentClients() {
        ClientDTO[] clients = restTemplate.getForObject(CUSTOMER_URL, ClientDTO[].class);
        if (clients == null) return new ArrayList<>();

        return Arrays.stream(clients)
                .filter(c -> "RESTRICTED".equalsIgnoreCase(c.getStatus()) || (c.getBalance() != null && c.getBalance() > 0))
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
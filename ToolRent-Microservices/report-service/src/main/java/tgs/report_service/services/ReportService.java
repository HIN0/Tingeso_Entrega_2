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

    private final String LOAN_ACTIVE_URL = "http://loan-service/api/loans/active"; 
    private final String LOAN_ALL_URL = "http://loan-service/api/loans";
    private final String CUSTOMER_URL = "http://customer-service/api/clients";
    
    public List<LoanDTO> getActiveLoans() {
        // Ahora es mucho más rápido y ligero
        LoanDTO[] loans = restTemplate.getForObject(LOAN_ACTIVE_URL, LoanDTO[].class);
        if (loans == null) return new ArrayList<>();
        return Arrays.asList(loans);
    }

    public List<ClientDTO> getDelinquentClients() {
        ClientDTO[] clients = restTemplate.getForObject(CUSTOMER_URL, ClientDTO[].class);
        if (clients == null) return new ArrayList<>();

        return Arrays.stream(clients)
                .filter(c -> "RESTRICTED".equalsIgnoreCase(c.getStatus()) && (c.getBalance() != null && c.getBalance() > 0))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getToolRanking() {
        // Para ranking seguimos necesitando el histórico
        LoanDTO[] loans = restTemplate.getForObject(LOAN_ALL_URL, LoanDTO[].class);
        if (loans == null) return new ArrayList<>();

        Map<Long, Long> frequencyMap = Arrays.stream(loans)
                .collect(Collectors.groupingBy(LoanDTO::getToolId, Collectors.counting()));

        List<Map<String, Object>> ranking = new ArrayList<>();
        
        for (Map.Entry<Long, Long> entry : frequencyMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("toolId", entry.getKey());
            item.put("loansCount", entry.getValue());
            ranking.add(item);
        }

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
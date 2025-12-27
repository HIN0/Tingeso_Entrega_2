package tgs.report_service.models;
// import lombok.Data; 

public class ReportSummaryDTO {
    
    private int activeLoansCount;
    private int delinquentClientsCount;

    // 1. Constructor Vacio (Necesario para Spring)
    public ReportSummaryDTO() {
    }

    // 2. Constructor con Argumentos (Opcional, pero útil)
    public ReportSummaryDTO(int activeLoansCount, int delinquentClientsCount) {
        this.activeLoansCount = activeLoansCount;
        this.delinquentClientsCount = delinquentClientsCount;
    }

    // 3. GETTERS Y SETTERS MANUALES (Esto arregla tu error)
    
    public int getActiveLoansCount() {
        return activeLoansCount;
    }

    public void setActiveLoansCount(int activeLoansCount) {
        this.activeLoansCount = activeLoansCount;
    }

    public int getDelinquentClientsCount() {
        return delinquentClientsCount;
    }

    public void setDelinquentClientsCount(int delinquentClientsCount) {
        this.delinquentClientsCount = delinquentClientsCount;
    }
}
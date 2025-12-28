package tgs.inventory_service.entities;

public enum ToolStatus {
    AVAILABLE,      // Disponible
    LOANED,         // Prestada
    REPAIRING,      // En reparación
    DECOMMISSIONED, // Dada de baja
    MANUAL_INCREASE, // Aumento manual de stock
    MANUAL_DECREASE // Disminución manual de stock
}
package tgs.inventory_service.entities;

public enum ToolStatus {
    AVAILABLE,      // Disponible
    LOANED,         // Prestada
    REPAIRING,      // En reparación
    DECOMMISSIONED, // Dada de baja
    MANUAL_DECREASE // Disminución manual de stock
}
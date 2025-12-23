package tgs.report_service.models;

import lombok.Data;

@Data
public class TariffDTO {
    private Integer dailyRentFee;
    private Integer dailyLateFee;
    private Integer repairFee;
}
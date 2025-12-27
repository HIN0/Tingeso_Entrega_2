package tgs.report_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffDTO {
    private Integer dailyRentFee;
    private Integer dailyLateFee;
    private Integer repairFee;
}
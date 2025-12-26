package tgs.loan_service.models;

import lombok.Data;

@Data
public class TariffDTO {
    private int dailyRentFee;
    private int dailyLateFee;
}
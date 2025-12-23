package tgs.tariff_service.services;

import tgs.tariff_service.entities.TariffEntity;
import tgs.tariff_service.repositories.TariffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TariffService {

    @Autowired
    private TariffRepository tariffRepository;

    // Obtener la tarifa actual (o crear una por defecto si está vacía)
    public TariffEntity getCurrentTariff() {
        List<TariffEntity> tariffs = tariffRepository.findAll();
        if (tariffs.isEmpty()) {
            // Crear tarifa por defecto si no existe
            TariffEntity defaultTariff = TariffEntity.builder()
                    .dailyRentFee(1500)
                    .dailyLateFee(5000)
                    .repairFee(10000)
                    .build();
            return tariffRepository.save(defaultTariff);
        }
        return tariffs.get(0);
    }

    // Actualizar tarifas
    public TariffEntity updateTariff(TariffEntity newValues) {
        TariffEntity current = getCurrentTariff();
        current.setDailyRentFee(newValues.getDailyRentFee());
        current.setDailyLateFee(newValues.getDailyLateFee());
        current.setRepairFee(newValues.getRepairFee());
        return tariffRepository.save(current);
    }
}
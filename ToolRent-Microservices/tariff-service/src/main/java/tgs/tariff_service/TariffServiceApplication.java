package tgs.tariff_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TariffServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TariffServiceApplication.class, args);
	}
}
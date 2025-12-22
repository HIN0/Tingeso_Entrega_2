package tgs.discovery_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer; // Ojo a este import

@SpringBootApplication
@EnableEurekaServer // <--- ESTA ES LA CLAVE. No uses @EnableDiscoveryClient aquí.
public class DiscoveryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryServiceApplication.class, args);
	}

}
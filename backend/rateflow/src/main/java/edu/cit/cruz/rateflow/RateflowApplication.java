package edu.cit.cruz.rateflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableJpaRepositories(basePackages = "edu.cit.cruz.rateflow.repository")
public class RateflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(RateflowApplication.class, args);
	}

}

package org.embed;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class})
public class lostArkApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(lostArkApiApplication.class, args);
	}

}


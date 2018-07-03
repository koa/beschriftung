package ch.teamkoenig.tool.beschriftung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.spring.annotation.EnableVaadin;

@EnableVaadin
@SpringBootApplication
public class App {
	public static void main(final String[] args) {
		SpringApplication.run(App.class, args);
	}
}

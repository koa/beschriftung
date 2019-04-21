package ch.teamkoenig.tool.beschriftung;

import com.vaadin.spring.annotation.EnableVaadin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableVaadin
@SpringBootApplication
public class App {
  public static void main(final String[] args) {
    SpringApplication.run(App.class, args);
  }
}

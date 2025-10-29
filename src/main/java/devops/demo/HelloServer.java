package devops.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class HelloServer {
    public static void main(String[] args) {
        SpringApplication.run(HelloServer.class, args);
    }

    @RestController
    static class HelloController {
        @GetMapping("/")
        public String hello() {
            return "Hello from Spring Boot on EC2!";
        }
    }
}

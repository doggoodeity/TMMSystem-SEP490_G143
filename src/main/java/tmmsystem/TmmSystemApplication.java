package tmmsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;


@CrossOrigin(origins = "*")
@SpringBootApplication
public class TmmSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmmSystemApplication.class, args);
    }

}

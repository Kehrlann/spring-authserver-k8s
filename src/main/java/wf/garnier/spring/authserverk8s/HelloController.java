package wf.garnier.spring.authserverk8s;

import java.security.interfaces.RSAKey;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Bienvenue to Spring One!";
    }
}

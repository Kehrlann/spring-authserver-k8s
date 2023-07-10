package wf.garnier.springone.authserverk8s.authserverk8s;

import java.security.interfaces.RSAKey;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

    @GetMapping("/")
    public String hello() {
        return """
                <html>
                    <head>
                        <title>Spring Auth Server on Kubernetes</title>
                    </head>
                    <body>
                    <h1>Bienvenue to Spring One!</h1>
                    <p><a href="http://token-viewer.127.0.0.1.nip.io/">Do an OAuth2 Login!</a></p>
                    </body>
                </html>
                """;
    }
}

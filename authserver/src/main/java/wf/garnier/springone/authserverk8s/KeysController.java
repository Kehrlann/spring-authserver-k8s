package wf.garnier.springone.authserverk8s;

import java.security.KeyPair;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class KeysController {
    private final KeyRepository keyRepository;
    private final JWKSource<SecurityContext> jwkSource;
    private final JWKSelector jwkSelector;

    public KeysController(KeyRepository keyRepository, JWKSource<SecurityContext> jwkSource) {
        this.keyRepository = keyRepository;
        this.jwkSource = jwkSource;
        this.jwkSelector = new JWKSelector(new JWKMatcher.Builder().build());
    }

    @GetMapping(value = "/manage-keys")
    public String listKeys(Model model) {
        try {
            List<JWK> jwks = this.jwkSource.get(this.jwkSelector, null);
            if (!CollectionUtils.isEmpty(jwks)) {
                model.addAttribute("keys", jwks);
            }
        } catch (Exception ex) {
            // TODO
        }

        return "manage-keys";
    }

    @PostMapping("/generate-key")
    public String generateKey(Model model) {
        KeyPair keyPair = KeyGeneratorUtils.generateRsaKey();
        KeyRepository.AsymmetricKey asymmetricKey = new KeyRepository.AsymmetricKey(
                UUID.randomUUID().toString(), Instant.now(), keyPair.getPrivate(), keyPair.getPublic());
        this.keyRepository.save(asymmetricKey);
        return listKeys(model);
    }

}
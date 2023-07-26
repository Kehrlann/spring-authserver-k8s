package wf.garnier.springone.authserverk8s.authserverk8s;

import java.util.List;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KeysController {
    private final JWKSource<SecurityContext> jwkSource;
    private final JWKSelector jwkSelector;

    public KeysController(JWKSource<SecurityContext> jwkSource) {
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

}
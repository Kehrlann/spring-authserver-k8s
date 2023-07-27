package wf.garnier.springone.authserverk8s;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.util.Assert;

public class DefaultJwkSource implements JWKSource<SecurityContext>  {
    private final KeyRepository keyRepository;

    public DefaultJwkSource(KeyRepository keyRepository) {
        Assert.notNull(keyRepository, "keyRepository cannot be null");
        this.keyRepository = keyRepository;
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
        List<? extends KeyRepository.Key> keys = this.keyRepository.getKeys();
        List<JWK> jwks = rsaJwks(keys);
        return jwkSelector.select(new JWKSet(jwks));
    }

    private static List<JWK> rsaJwks(List<? extends KeyRepository.Key> keys) {
        List<JWK> rsaJwks = new ArrayList<>();

        keys.forEach(key -> {
            if (key instanceof KeyRepository.AsymmetricKey asymmetricKey &&
                    asymmetricKey.getPublicKey() instanceof RSAPublicKey) {

                RSAPublicKey publicKey = (RSAPublicKey) asymmetricKey.getPublicKey();
                RSAPrivateKey privateKey = (RSAPrivateKey) asymmetricKey.getPrivateKey();
                RSAKey rsaJwk = new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .keyID(asymmetricKey.getId())
                        .issueTime(Date.from(asymmetricKey.getCreated()))
                        .build();
                rsaJwks.add(rsaJwk);
            }
        });

        return rsaJwks;
    }

}
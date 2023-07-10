package wf.garnier.springone.authserverk8s.authserverk8s;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

class StaticJwkRepository implements JWKSource<SecurityContext>  {

    private RSAKey rsaKey = getRsaKey();

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
        return List.of(rsaKey);
    }

    private RSAKey getRsaKey() {
        if (rsaKey != null) {
            return rsaKey;
        }

        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey)
                .keyID("in-memory-key")
                .build();
        return rsaKey;
    }

    private KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

}

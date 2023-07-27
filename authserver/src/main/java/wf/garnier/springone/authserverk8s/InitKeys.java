package wf.garnier.springone.authserverk8s;

import java.security.KeyPair;
import java.time.Instant;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class InitKeys implements ApplicationRunner {
    private final KeyRepository keyRepository;

    InitKeys(KeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        KeyPair keyPair = KeyGeneratorUtils.generateRsaKey();
        KeyRepository.AsymmetricKey asymmetricKey = new KeyRepository.AsymmetricKey(
                "default-" + UUID.randomUUID(), Instant.now(), keyPair.getPrivate(), keyPair.getPublic());
        this.keyRepository.save(asymmetricKey);
    }

}
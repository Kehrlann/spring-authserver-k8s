package wf.garnier.springone.authserverk8s;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.util.Assert;

public interface KeyRepository {

    <T extends Key> T findKey(String id);

    List<? extends Key> getKeys();

    void delete(String id);

    <T extends Key> void save(T key);

    abstract class Key {
        private final String id;
        private final Instant created;

        protected Key (String id, Instant created) {
            Assert.hasText(id, "id cannot be empty");
            Assert.notNull(created, "created cannot be null");
            this.id = id;
            this.created = created;
        }

        public String getId() {
            return this.id;
        }

        public Instant getCreated() {
            return this.created;
        }

    }

    class SymmetricKey extends Key {
        private final SecretKey secretKey;

        SymmetricKey(String id, Instant created, SecretKey secretKey) {
            super(id, created);
            Assert.notNull(secretKey, "secretKey cannot be null");
            this.secretKey = secretKey;
        }

        public SecretKey getSecretKey() {
            return this.secretKey;
        }

    }

    class AsymmetricKey extends Key {
        private final PrivateKey privateKey;
        private final PublicKey publicKey;

        AsymmetricKey(String id, Instant created, PrivateKey privateKey, PublicKey publicKey) {
            super(id, created);
            Assert.notNull(privateKey, "privateKey cannot be null");
            Assert.notNull(publicKey, "publicKey cannot be null");
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public PrivateKey getPrivateKey() {
            return this.privateKey;
        }

        public PublicKey getPublicKey() {
            return this.publicKey;
        }

    }

}
package wf.garnier.springone.authserverk8s;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKeyRepository implements KeyRepository {
    private final Map<String, Key> keys = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Key> T findKey(String id) {
        return (T) this.keys.get(id);
    }

    @Override
    public List<? extends Key> getKeys() {
        List<? extends Key> result = new ArrayList<>(this.keys.values());
        result.sort(Comparator.comparing(Key::getCreated).reversed());
        return result;
    }

    @Override
    public void delete(String id) {
        this.keys.remove(id);
    }

    @Override
    public <T extends Key> void save(T key) {
        this.keys.put(key.getId(), key);
    }

}
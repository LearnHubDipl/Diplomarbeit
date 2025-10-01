// src/main/java/at/learnhub/repository/TeacherRepository.java
package at.learnhub.repository;

import at.learnhub.model.Teacher;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class TeacherRepository {

    private final Map<Long, Teacher> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(100L);

    @PostConstruct
    void init() {
        save(new Teacher(null, "Klassenvorstand", "kv@schule.at"));
        save(new Teacher(null, "Prof. Muster", "muster@schule.at"));
        save(new Teacher(null, "Mag. Beispiel", "beispiel@schule.at"));
    }

    public List<Teacher> listAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Teacher::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Teacher save(Teacher t) {
        if (t.getId() == null) {
            t.setId(seq.incrementAndGet());
        }
        store.put(t.getId(), t);
        return t;
    }

    public Optional<Teacher> update(Long id, Teacher patch) {
        Teacher current = store.get(id);
        if (current == null) return Optional.empty();
        if (patch.getName() != null && !patch.getName().isBlank()) current.setName(patch.getName());
        if (patch.getEmail() != null) current.setEmail(patch.getEmail());
        return Optional.of(current);
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}

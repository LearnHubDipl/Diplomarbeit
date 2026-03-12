package at.learnhub.model;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Generic CSV entity importer for loading JPA entities from CSV files on application startup.
 *
 * Strategy to avoid OptimisticLockExceptions and missing required relations:
 * 1) Pass 1: instantiate entities from CSV (no relations, do not persist).
 *    - Capture optional CSV primary key column "id" into an in-memory pk map (but DO NOT set on entity).
 * 2) Pass 2: resolve relations among the in-memory objects.
 *    - When a relation column like subjectId/subject_id/... is seen, first try to resolve by CSV pk value
 *      (if that target CSV provided an "id" column and we captured it). Fallback to row index.
 * 3) Pass 3: persist entities in dependency order (parents before children).
 */
@ApplicationScoped
public class GenericEntityCsvImporter {

    private static final String ENTITY_PACKAGE = "at.learnhub.model";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Inject
    EntityManager em;

    @Transactional
    void onStartup(@Observes StartupEvent event) {
        try {
            importAllCsvEntities();
        } catch (IOException e) {
            throw new RuntimeException("Error importing CSVs", e);
        }
    }

    @Transactional
    public void importAllCsvEntities() throws IOException {
        String folder = System.getenv().getOrDefault("MOCKDATA_PATH", "mock-data");
        Path folderPath = Paths.get(folder);

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            System.out.println("No mock-data folder found at: " + folderPath.toAbsolutePath());
            return;
        }

        System.out.println("Importing CSV files from: " + folderPath.toAbsolutePath());

        // className -> (rowIndex -> entity)
        Map<String, Map<Long, Object>> entityCache = new HashMap<>();
        // className -> (csvPkValue -> entity)  (csvPkValue is the string from the CSV "id" column if present)
        Map<String, Map<String, Object>> pkCache = new HashMap<>();
        Set<String> discoveredClasses = new HashSet<>();

        // -------- Pass 1: build entities, record CSV primary keys (but don't persist) --------
        try (Stream<Path> paths = Files.walk(folderPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String baseName = fileName.substring(0, fileName.length() - 4);
                        String className = ENTITY_PACKAGE + "." + toPascalCase(baseName);

                        LoadResult result = loadEntitiesWithoutRelations(path, className);
                        entityCache.put(className, result.entitiesByIndex);
                        if (!result.entitiesByCsvPk.isEmpty()) {
                            pkCache.put(className, result.entitiesByCsvPk);
                        }
                        discoveredClasses.add(className);
                    });
        }

        // -------- Pass 2: set up relations --------
        try (Stream<Path> paths = Files.walk(folderPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String baseName = fileName.substring(0, fileName.length() - 4);
                        String className = ENTITY_PACKAGE + "." + toPascalCase(baseName);

                        Map<Long, Object> entities = entityCache.get(className);
                        if (entities != null) {
                            loadRelations(path, className, entities, entityCache, pkCache);
                        }
                    });
        }

        // -------- Pass 3: persist in dependency order --------
        List<String> order = computeDependencyOrder(discoveredClasses);
        for (String className : order) {
            Map<Long, Object> entities = entityCache.get(className);
            if (entities == null) continue;
            for (Object entity : entities.values()) {
                if (!em.contains(entity)) em.persist(entity);
            }
        }

        em.flush();
        System.out.println("Import completed!");
    }

    /** Container for pass-1 results. */
    private static class LoadResult {
        final Map<Long, Object> entitiesByIndex = new LinkedHashMap<>();
        final Map<String, Object> entitiesByCsvPk = new HashMap<>();
    }

    /** Loads entities (no relations); captures optional CSV primary key column named "id". */
    private LoadResult loadEntitiesWithoutRelations(Path path, String className) {
        LoadResult result = new LoadResult();
        try (Reader reader = Files.newBufferedReader(path)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            Class<?> entityClass = Class.forName(className);

            long index = 1;
            for (CSVRecord record : parser) {
                Object entity = entityClass.getDeclaredConstructor().newInstance();
                Map<String, String> recordMap = record.toMap();

                // Capture CSV pk value if present (but do not set it on the entity)
                String csvPk = null;
                for (Map.Entry<String, String> e : recordMap.entrySet()) {
                    String header = e.getKey() == null ? null : e.getKey().trim();
                    if (header != null && header.equalsIgnoreCase("id")) {
                        String v = e.getValue();
                        if (v != null && !v.trim().isEmpty() && !v.trim().equalsIgnoreCase("null")) {
                            csvPk = v.trim();
                        }
                    }
                }

                for (Map.Entry<String, String> entry : recordMap.entrySet()) {
                    String csvField = entry.getKey();
                    String csvValue = entry.getValue();
                    if (csvField != null) csvField = csvField.trim();

                    // ignore primary key and relation carrier columns
                    if (isPrimaryKey(csvField) || isRelationCarrier(csvField)) continue;

                    try {
                        Field field = entityClass.getDeclaredField(csvField);
                        field.setAccessible(true);
                        Object convertedValue = convertValue(field.getType(), csvValue);
                        field.set(entity, convertedValue);
                    } catch (NoSuchFieldException e) {
                        // ignore unknown CSV columns
                    }
                }

                result.entitiesByIndex.put(index++, entity);
                if (csvPk != null) {
                    result.entitiesByCsvPk.put(csvPk, entity);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load entities from file: " + path.getFileName(), e);
        }
        return result;
    }

    /** Loads and sets relations using either CSV pk values (preferred) or row indices.
     *  Supports both carrier columns (...id / ...ids) and plain relation field names. */
    private void loadRelations(Path path, String className, Map<Long, Object> entities,
                               Map<String, Map<Long, Object>> entityCache,
                               Map<String, Map<String, Object>> pkCache) {
        try (Reader reader = Files.newBufferedReader(path)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            Class<?> entityClass = Class.forName(className);

            long index = 1;
            for (CSVRecord record : parser) {
                Object entity = entities.get(index);
                if (entity == null) { index++; continue; }

                Map<String, String> recordMap = record.toMap();
                for (Map.Entry<String, String> entry : recordMap.entrySet()) {
                    String csvField = entry.getKey();
                    String csvValue = entry.getValue();
                    if (csvField != null) csvField = csvField.trim();
                    if (csvValue == null || csvValue.isBlank()) continue;

                    // Never treat primary key column as a relation
                    if (isPrimaryKey(csvField)) continue;

                    try {
                        // A) classic carrier columns ...id / ...ids
                        if (isRelationCarrier(csvField)) {
                            String relationFieldName = normalizeRelationFieldName(csvField);
                            if (relationFieldName == null || relationFieldName.isBlank()) continue; // guard
                            setRelation(entityClass, entity, relationFieldName, csvValue, entityCache, pkCache);
                            continue;
                        }

                        // B) plain field name equals relation field (e.g., "subject" or "students")
                        if (isRelationField(entityClass, csvField)) {
                            setRelation(entityClass, entity, csvField, csvValue, entityCache, pkCache);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(
                                "Failed to resolve relation '" + csvField + "' for " + className
                                        + " row " + index + " (value='" + csvValue + "')", ex);
                    }
                }
                index++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load relations from file: " + path.getFileName(), e);
        }
    }

    private List<String> computeDependencyOrder(Set<String> classes) {
        Map<String, Set<String>> deps = new HashMap<>();    // class -> dependsOn
        Map<String, Set<String>> reverse = new HashMap<>(); // class -> dependedBy

        for (String className : classes) {
            deps.putIfAbsent(className, new HashSet<>());
            reverse.putIfAbsent(className, new HashSet<>());
            try {
                Class<?> cls = Class.forName(className);
                for (Field f : cls.getDeclaredFields()) {
                    if (f.isAnnotationPresent(ManyToOne.class) || f.isAnnotationPresent(OneToOne.class)) {
                        Class<?> related = f.getType();
                        String relName = related.getName();
                        if (classes.contains(relName) && !relName.equals(className)) {
                            deps.get(className).add(relName);
                            reverse.computeIfAbsent(relName, k -> new HashSet<>()).add(className);
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {}
        }

        Deque<String> q = new ArrayDeque<>();
        for (String c : classes) {
            if (deps.getOrDefault(c, Collections.emptySet()).isEmpty()) q.add(c);
        }
        List<String> order = new ArrayList<>();
        while (!q.isEmpty()) {
            String n = q.removeFirst();
            order.add(n);
            for (String m : reverse.getOrDefault(n, Collections.emptySet())) {
                Set<String> d = deps.get(m);
                if (d != null) {
                    d.remove(n);
                    if (d.isEmpty()) q.add(m);
                }
            }
        }
        if (order.size() < classes.size()) {
            Set<String> remaining = new HashSet<>(classes);
            remaining.removeAll(order);
            order.addAll(remaining);
        }
        return order;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> ensureCollectionOnField(Object target, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        Object current = field.get(target);
        if (current instanceof Collection<?>) {
            return (Collection<Object>) current;
        }
        Collection<Object> created = newCollectionFor(field.getType());
        field.set(target, created);
        return created;
    }

    private Collection<Object> newCollectionFor(Class<?> fieldType) {
        if (Set.class.isAssignableFrom(fieldType)) return new HashSet<>();
        if (List.class.isAssignableFrom(fieldType)) return new ArrayList<>();
        if (Collection.class.isAssignableFrom(fieldType)) return new ArrayList<>();
        return new ArrayList<>();
    }

    private Class<?> getCollectionGenericType(Field field) {
        try {
            java.lang.reflect.ParameterizedType type = (java.lang.reflect.ParameterizedType) field.getGenericType();
            return (Class<?>) type.getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine generic type for field: " + field.getName(), e);
        }
    }

    private Object convertValue(Class<?> type, String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("null")) return null;
        if (type == Long.class || type == long.class) return Long.parseLong(value.trim());
        if (type == Integer.class || type == int.class) return Integer.parseInt(value.trim());
        if (type == Double.class || type == double.class) return Double.parseDouble(value.trim());
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value.trim());
        if (type == LocalDate.class) return LocalDate.parse(value.trim(), DATE_FORMATTER);
        if (type == LocalDateTime.class) return LocalDateTime.parse(value.trim(), DATETIME_FORMATTER);
        if (type.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<Enum> enumType = (Class<Enum>) type;
            String enumValue = value.trim();
            for (Enum constant : enumType.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(enumValue)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid enum value: " + value + " for enum " + type.getSimpleName());
        }
        return value;
    }

    private Long parseLongSafe(String s) {
        if (s == null) return null;
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return null; }
    }

    private String toPascalCase(String text) {
        String[] parts = text.split("[-_]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }
            }
        }
        return sb.toString();
    }

    // --- Helpers for relation column detection / normalization ---
    private boolean isPrimaryKey(String csvField) {
        return csvField != null && csvField.trim().equalsIgnoreCase("id");
    }

    /**
     * Recognize relation carrier columns but never treat the plain primary key "id" as a relation.
     * Requires at least one character before the (optional) separator and trailing id/ids.
     */
    private boolean isRelationCarrier(String csvField) {
        if (csvField == null) return false;
        String f = csvField.trim();
        if (isPrimaryKey(f)) return false; // "id" is never a relation
        return f.matches("(?i).+[_-]?ids$") || f.matches("(?i).+[_-]?id$");
    }

    private String normalizeRelationFieldName(String csvField) {
        if (csvField == null) return null;
        String f = csvField.trim();
        f = f.replaceAll("(?i)(?:[_-]?ids|[_-]?id)$", "");
        String[] parts = f.split("[-_]");
        if (parts.length <= 1) return f;
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }

    /** True if a declared field on the entity is a relation. */
    private boolean isRelationField(Class<?> entityClass, String fieldName) {
        if (fieldName == null) return false;
        try {
            Field f = entityClass.getDeclaredField(fieldName.trim());
            return f.isAnnotationPresent(ManyToOne.class)
                    || f.isAnnotationPresent(OneToOne.class)
                    || f.isAnnotationPresent(ManyToMany.class);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /** Set relation for ManyToOne/OneToOne/ManyToMany; CSV value can be PK or 1-based row index. */
    @SuppressWarnings("unchecked")
    private void setRelation(Class<?> entityClass, Object entity, String relationFieldName, String csvValue,
                             Map<String, Map<Long, Object>> entityCache, Map<String, Map<String, Object>> pkCache) throws Exception {
        Field relationField = entityClass.getDeclaredField(relationFieldName);
        relationField.setAccessible(true);

        if (relationField.isAnnotationPresent(ManyToOne.class) || relationField.isAnnotationPresent(OneToOne.class)) {
            Class<?> relatedClass = relationField.getType();
            String relatedClassName = relatedClass.getName();

            Object relatedEntity = null;
            Map<String, Object> relatedByPk = pkCache.get(relatedClassName);
            if (relatedByPk != null) relatedEntity = relatedByPk.get(csvValue.trim());
            if (relatedEntity == null) {
                Long idx = parseLongSafe(csvValue);
                Map<Long, Object> relatedByIndex = entityCache.get(relatedClassName);
                if (idx != null && relatedByIndex != null) relatedEntity = relatedByIndex.get(idx);
            }
            if (relatedEntity != null) relationField.set(entity, relatedEntity);
            return;
        }

        if (relationField.isAnnotationPresent(ManyToMany.class)) {
            Class<?> elementType = getCollectionGenericType(relationField);
            String relatedClassName = elementType.getName();
            Map<String, Object> relatedByPk = pkCache.get(relatedClassName);
            Map<Long, Object> relatedByIndex = entityCache.get(relatedClassName);

            Collection<Object> collection = ensureCollectionOnField(entity, relationField);
            String[] tokens = csvValue.split(";");
            for (String token : tokens) {
                if (token == null || token.isBlank()) continue;
                Object rel = null;
                String key = token.trim();
                if (relatedByPk != null) rel = relatedByPk.get(key);
                if (rel == null) {
                    Long idx = parseLongSafe(key);
                    if (idx != null && relatedByIndex != null) rel = relatedByIndex.get(idx);
                }
                if (rel != null) collection.add(rel);
            }
        }
    }
}
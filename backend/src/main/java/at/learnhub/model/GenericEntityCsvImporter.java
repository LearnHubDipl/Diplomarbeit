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
 * <p>
 * This component:
 * <ul>
 *     <li>Reads CSV files from a specified directory</li>
 *     <li>Maps records to entity objects using reflection</li>
 *     <li>Handles simple field population and relationship resolution</li>
 *     <li>Persists all entities into the database</li>
 * </ul>
 */
@ApplicationScoped
public class GenericEntityCsvImporter {

    private static final String DATA_FOLDER = "/app/mock-data";
    private static final String ENTITY_PACKAGE = "at.learnhub.model";

    @Inject
    EntityManager em;

    private final Map<String, Map<Long, Object>> entityStore = new HashMap<>();

    /**
     * Triggered on application startup to begin the CSV import process.
     *
     * @param event the Quarkus startup event
     */
    @Transactional
    void onStartup(@Observes StartupEvent event) {
        try {
            importAllCsvEntities();
        } catch (IOException e) {
            throw new RuntimeException("Error importing CSVs", e);
        }
    }

    /**
     * Imports all CSV entities from the configured directory. This method:
     * <ol>
     *     <li>Loads all CSVs into entity instances (without setting relations)</li>
     *     <li>Resolves relationships between entities</li>
     *     <li>Persists the resulting graph to the database</li>
     * </ol>
     *
     * @throws IOException if CSV reading fails
     */
    @Transactional
    public void importAllCsvEntities() throws IOException {
        String folder = System.getenv().getOrDefault("MOCKDATA_PATH", "mock-data");
        Path folderPath = Paths.get(folder);

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            System.out.println("No mock-data folder found at: " + folderPath.toAbsolutePath());
            return;
        }

        System.out.println("Importing CSV files from: " + folderPath.toAbsolutePath());
        Map<String, Map<Long, Object>> entityCache = new HashMap<>();

        try (Stream<Path> paths = Files.walk(folderPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String baseName = fileName.substring(0, fileName.length() - 4);
                        String className = ENTITY_PACKAGE + "." + toPascalCase(baseName);

                        Map<Long, Object> entities = loadEntitiesWithoutRelations(path, className);
                        entityCache.put(className, entities);
                    });
        }

        try (Stream<Path> paths = Files.walk(folderPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String baseName = fileName.substring(0, fileName.length() - 4);
                        String className = ENTITY_PACKAGE + "." + toPascalCase(baseName);

                        Map<Long, Object> entities = entityCache.get(className);
                        if (entities != null) {
                            loadRelations(path, className, entities, entityCache);
                        }
                    });
        }

        for (Map<Long, Object> entities : entityCache.values()) {
            for (Object entity : entities.values()) {
                em.persist(entity);
            }
        }

        em.flush();
        System.out.println("Import completed!");
    }

    /**
     * Loads entity instances from a CSV file, excluding relation fields.
     *
     * @param path      the path to the CSV file
     * @param className the fully-qualified class name of the entity
     * @return a map of temporary index IDs to instantiated entities
     */
    private Map<Long, Object> loadEntitiesWithoutRelations(Path path, String className) {
        Map<Long, Object> entities = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            Class<?> entityClass = Class.forName(className);

            long index = 1;
            for (CSVRecord record : parser) {
                Object entity = entityClass.getDeclaredConstructor().newInstance();
                Map<String, String> recordMap = record.toMap();

                for (Map.Entry<String, String> entry : recordMap.entrySet()) {
                    String csvField = entry.getKey();
                    String csvValue = entry.getValue();

                    if (csvField.endsWith("Id") || csvField.endsWith("Ids")) continue;

                    try {
                        Field field = entityClass.getDeclaredField(csvField);
                        field.setAccessible(true);
                        Object convertedValue = convertValue(field.getType(), csvValue);
                        field.set(entity, convertedValue);
                    } catch (NoSuchFieldException e) {
                        // Ignore unknown fields
                    }
                }

                entities.put(index++, entity);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load entities from file: " + path.getFileName(), e);
        }
        return entities;
    }

    /**
     * Loads and sets relations for the entities using CSV relation fields (e.g., examId, selectedAnswersIds).
     *
     * @param path         the path to the CSV file
     * @param className    the fully-qualified entity class name
     * @param entities     the entity instances to populate
     * @param entityCache  the cache of all imported entities
     */
    private void loadRelations(Path path, String className, Map<Long, Object> entities,
                               Map<String, Map<Long, Object>> entityCache) {
        try (Reader reader = Files.newBufferedReader(path)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            Class<?> entityClass = Class.forName(className);

            long index = 1;
            boolean allRelationsSet = true;

            for (CSVRecord record : parser) {
                Object entity = entities.get(index);
                if (entity == null) {
                    index++;
                    continue;
                }

                Map<String, String> recordMap = record.toMap();

                for (Map.Entry<String, String> entry : recordMap.entrySet()) {
                    String csvField = entry.getKey();
                    String csvValue = entry.getValue();

                    if ((csvField.endsWith("Id") || csvField.endsWith("Ids")) && csvValue != null && !csvValue.isEmpty()) {
                        String relationFieldName = csvField.replaceFirst("Ids?$", "");

                        try {
                            Field relationField = entityClass.getDeclaredField(relationFieldName);
                            relationField.setAccessible(true);

                            if (relationField.isAnnotationPresent(ManyToOne.class)
                                    || relationField.isAnnotationPresent(OneToOne.class)) {

                                Class<?> relatedClass = relationField.getType();
                                Map<Long, Object> relatedEntities = entityCache.get(relatedClass.getName());

                                Long relatedId = Long.parseLong(csvValue);
                                Object relatedEntity = relatedEntities.get(relatedId);
                                relationField.set(entity, relatedEntity);

                            } else if (relationField.isAnnotationPresent(ManyToMany.class)) {
                                Class<?> collectionType = getCollectionGenericType(relationField);
                                Map<Long, Object> relatedEntities = entityCache.get(collectionType.getName());

                                Collection<Object> collection = getOrCreateCollection(relationField.getType());
                                relationField.set(entity, collection);

                                String[] relatedIds = csvValue.split(";");
                                for (String idStr : relatedIds) {
                                    Long relatedId = Long.parseLong(idStr.trim());
                                    Object relatedEntity = relatedEntities.get(relatedId);
                                    collection.add(relatedEntity);
                                }
                            }

                        } catch (NoSuchFieldException e) {
                            allRelationsSet = false;
                        }
                    }
                }
                index++;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load relations from file: " + path.getFileName(), e);
        }
    }

    /**
     * Returns the generic type of a collection field.
     *
     * @param field the collection field
     * @return the class of the collection's generic type
     */
    private Class<?> getCollectionGenericType(Field field) {
        try {
            java.lang.reflect.ParameterizedType type = (java.lang.reflect.ParameterizedType) field.getGenericType();
            return (Class<?>) type.getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine generic type for field: " + field.getName(), e);
        }
    }

    /**
     * Instantiates a collection for use in ManyToMany relations.
     *
     * @param fieldType the type of the field (e.g., List.class)
     * @return a concrete collection instance (e.g., ArrayList)
     */
    private Collection<Object> getOrCreateCollection(Class<?> fieldType) {
        if (fieldType.isAssignableFrom(List.class)) return new ArrayList<>();
        if (fieldType.isAssignableFrom(Set.class)) return new HashSet<>();
        return new ArrayList<>();
    }

    /**
     * Converts a string value to the expected Java type for field population.
     *
     * @param type  the expected Java type
     * @param value the CSV string value
     * @return a parsed Java object
     */
    private Object convertValue(Class<?> type, String value) {
        if (value == null || value.isEmpty()) return null;
        if (type == Long.class || type == long.class) return Long.parseLong(value);
        if (type == Integer.class || type == int.class) return Integer.parseInt(value);
        if (type == Double.class || type == double.class) return Double.parseDouble(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
        if (type == LocalDate.class) return LocalDate.parse(value, DATE_FORMATTER);
        if (type == LocalDateTime.class) return LocalDateTime.parse(value, DATETIME_FORMATTER);
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

    /**
     * Converts snake-case or kebab-case file names to PascalCase class names.
     *
     * @param text the base name of the CSV file
     * @return the corresponding PascalCase entity class name
     */
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
}

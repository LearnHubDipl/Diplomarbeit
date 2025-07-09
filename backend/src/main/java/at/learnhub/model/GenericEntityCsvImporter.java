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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@ApplicationScoped
public class GenericEntityCsvImporter {

    private static final String DATA_FOLDER = "/mock-data";
    private static final String ENTITY_PACKAGE = "at.learnhub.model";

    @Inject
    EntityManager em;

    private final Map<String, Map<Long, Object>> entityStore = new HashMap<>();

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
        URL folderUrl = getClass().getResource(DATA_FOLDER);
        if (folderUrl == null) {
            System.out.println("No mock-data folder found");
            return;
        }

        Path folderPath;
        try {
            folderPath = Paths.get(folderUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid mock-data path", e);
        }

        Map<String, Map<Long, Object>> entityCache = new HashMap<>();

        // 1. load entities without relations and save them
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

        // 2. set relations using id of the csv
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

        // 3. persist finished entities
        for (Map<Long, Object> entities : entityCache.values()) {
            for (Object entity : entities.values()) {
                em.persist(entity);
            }
        }

        em.flush();
        System.out.println("Import completed!");
    }

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

                    // skip these fields because they are relations
                    if (csvField.endsWith("Id") || csvField.endsWith("Ids")) {
                        continue;
                    }

                    try {
                        Field field = entityClass.getDeclaredField(csvField);
                        field.setAccessible(true);
                        Object convertedValue = convertValue(field.getType(), csvValue);
                        field.set(entity, convertedValue);
                    } catch (NoSuchFieldException e) {
                        // ignore
                    }
                }
                entities.put(index++, entity);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load entities from file: " + path.getFileName(), e);
        }
        return entities;
    }

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
                    System.out.printf("Entity index %d not found for %s%n", index, className);
                    allRelationsSet = false;
                    index++;
                    continue;
                }

                Map<String, String> recordMap = record.toMap();

                for (Map.Entry<String, String> entry : recordMap.entrySet()) {
                    String csvField = entry.getKey();
                    String csvValue = entry.getValue();

                    if ((csvField.endsWith("Id") || csvField.endsWith("Ids")) && csvValue != null && !csvValue.isEmpty()) {
                        String relationFieldName = csvField.endsWith("Ids") ?
                                csvField.substring(0, csvField.length() - 3) :
                                csvField.substring(0, csvField.length() - 2);

                        try {
                            Field relationField = entityClass.getDeclaredField(relationFieldName);
                            relationField.setAccessible(true);

                            if (relationField.isAnnotationPresent(ManyToOne.class) || relationField.isAnnotationPresent(OneToOne.class)) {
                                // einfache ManyToOne / OneToOne Relation
                                Class<?> relatedClass = relationField.getType();
                                Map<Long, Object> relatedEntities = entityCache.get(relatedClass.getName());
                                if (relatedEntities == null) {
                                    System.out.printf("No cached entities found for related class %s%n", relatedClass.getSimpleName());
                                    allRelationsSet = false;
                                    continue;
                                }

                                Long relatedId = Long.parseLong(csvValue);
                                Object relatedEntity = relatedEntities.get(relatedId);
                                if (relatedEntity == null) {
                                    System.out.printf("Related entity %s with index %s not found for field %s%n",
                                            relatedClass.getSimpleName(), csvValue, relationFieldName);
                                    allRelationsSet = false;
                                } else {
                                    relationField.set(entity, relatedEntity);
                                }
                            } else if (relationField.isAnnotationPresent(ManyToMany.class)) {
                                // ManyToMany field
                                Class<?> collectionType = getCollectionGenericType(relationField);
                                Map<Long, Object> relatedEntities = entityCache.get(collectionType.getName());
                                if (relatedEntities == null) {
                                    System.out.printf("No cached entities found for related class %s%n", collectionType.getSimpleName());
                                    allRelationsSet = false;
                                    continue;
                                }

                                Collection<Object> collection = getOrCreateCollection(relationField.getType());
                                relationField.set(entity, collection);

                                String[] relatedIds = csvValue.split(";");
                                for (String idStr : relatedIds) {
                                    Long relatedId = Long.parseLong(idStr.trim());
                                    Object relatedEntity = relatedEntities.get(relatedId);
                                    if (relatedEntity == null) {
                                        System.out.printf("Related entity %s with index %s not found for field %s%n",
                                                collectionType.getSimpleName(), relatedId, relationFieldName);
                                        allRelationsSet = false;
                                    } else {
                                        collection.add(relatedEntity);
                                    }
                                }
                            } else {
                                System.out.printf("Field '%s' in %s is not a supported relation type%n",
                                        relationFieldName, entityClass.getSimpleName());
                                allRelationsSet = false;
                            }
                        } catch (NoSuchFieldException e) {
                            System.out.printf("Field '%s' not found in %s%n", relationFieldName, entityClass.getSimpleName());
                            allRelationsSet = false;
                        }
                    }
                }
                index++;
            }

            if (allRelationsSet) {
                System.out.printf("All relations successfully loaded for file: %s%n", path.getFileName());
            } else {
                System.out.printf("!! Some relations could not be set for file: %s%n", path.getFileName());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load relations from file: " + path.getFileName(), e);
        }
    }

    private Class<?> getCollectionGenericType(Field field) {
        try {
            java.lang.reflect.ParameterizedType stringListType = (java.lang.reflect.ParameterizedType) field.getGenericType();
            return (Class<?>) stringListType.getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine generic type of collection for field: " + field.getName(), e);
        }
    }

    private Collection<Object> getOrCreateCollection(Class<?> fieldType) {
        if (fieldType.isAssignableFrom(List.class)) {
            return new ArrayList<>();
        }
        if (fieldType.isAssignableFrom(Set.class)) {
            return new HashSet<>();
        }
        // Default fallback
        return new ArrayList<>();
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private Object convertValue(Class<?> type, String value) {
        if (value == null || value.isEmpty()) return null;

        if (type == Long.class || type == long.class) return Long.parseLong(value);
        if (type == Integer.class || type == int.class) return Integer.parseInt(value);
        if (type == Double.class || type == double.class) return Double.parseDouble(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
        if (type == LocalDate.class) return LocalDate.parse(value, DATE_FORMATTER);
        if (type == LocalDateTime.class) return LocalDateTime.parse(value, DATETIME_FORMATTER);
        if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value);

        return value;
    }

    private String toPascalCase(String text) {
        String[] parts = text.split("[-_]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }
            }
        }
        return sb.toString();
    }
}

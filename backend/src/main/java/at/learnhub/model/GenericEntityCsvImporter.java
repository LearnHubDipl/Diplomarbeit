package at.learnhub.model;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.transaction.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

@ApplicationScoped
public class GenericEntityCsvImporter {

    private static final String DATA_FOLDER = "/mock-data";
    private static final String ENTITY_PACKAGE = "at.learnhub.model";

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

        try (Stream<Path> paths = Files.walk(folderPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(this::importFile);
        }
    }

    private void importFile(Path path) {
        String fileName = path.getFileName().toString();
        String baseName = fileName.substring(0, fileName.length() - 4);
        String className = ENTITY_PACKAGE + "." + toPascalCase(baseName);

        try (Reader reader = Files.newBufferedReader(path)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            Class<?> entityClass = Class.forName(className);

            for (CSVRecord record : parser) {
                Object entity = entityClass.getDeclaredConstructor().newInstance();

                Map<String, String> recordMap = record.toMap();

                for (Map.Entry<String, String> entry : recordMap.entrySet()) {
                    String csvField = entry.getKey();
                    String csvValue = entry.getValue();

                    if (csvField.endsWith("Id")) {
                        String relationFieldName = csvField.substring(0, csvField.length() - 2);
                        try {
                            Field relationField = entityClass.getDeclaredField(relationFieldName);
                            relationField.setAccessible(true);

                            if (relationField.isAnnotationPresent(ManyToOne.class) || relationField.isAnnotationPresent(OneToOne.class)) {
                                Class<?> relatedClass = relationField.getType();

                                Object relatedEntity = em.find(relatedClass, convertValue(getIdType(relatedClass), csvValue));
                                relationField.set(entity, relatedEntity);
                                continue;
                            }
                        } catch (NoSuchFieldException e) {
                            System.out.println("Field " + csvField + " not found");
                        }
                    }

                    try {
                        Field field = entityClass.getDeclaredField(csvField);
                        field.setAccessible(true);
                        Object convertedValue = convertValue(field.getType(), csvValue);
                        field.set(entity, convertedValue);
                    } catch (NoSuchFieldException e) {
                        System.out.printf("⚠️  Field '%s' not found in %s%n", csvField, className);
                    }
                }

                em.persist(entity);
            }

            System.out.printf("✅ Imported %s%n", fileName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to import file: " + fileName, e);
        }
    }

    private Class<?> getIdType(Class<?> entityClass) {
        return Long.class;
    }

    private Object convertValue(Class<?> type, String value) {
        if (value == null || value.isEmpty()) return null;
        if (type == Long.class || type == long.class) return Long.parseLong(value);
        if (type == Integer.class || type == int.class) return Integer.parseInt(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
        if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value);
        return value;
    }

    private String toPascalCase(String kebabOrSnake) {
        String[] parts = kebabOrSnake.split("[-_]");
        return Arrays.stream(parts)
                .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1))
                .reduce("", String::concat);
    }
}
package com.fadhil.fashion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fadhil.fashion.model.ContactMessage;
import com.fadhil.fashion.model.LoginResponse;
import com.fadhil.fashion.model.OrderRequest;
import com.fadhil.fashion.model.Product;
import com.fadhil.fashion.model.User;
import com.fadhil.fashion.persistence.Database;
import com.fadhil.fashion.persistence.UserRecord;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final ObjectMapper objectMapper;
    private final Path dataFile;
    private final List<Product> products = List.of(
            new Product("satin-evening-dress", "Satin Evening Dress", "Women", "Elegant", 95000, "TZS"),
            new Product("smart-linen-shirt", "Smart Linen Shirt", "Men", "Daily wear", 52000, "TZS"),
            new Product("premium-denim-jacket", "Premium Denim Jacket", "Men", "Best seller", 78000, "TZS"),
            new Product("leather-crossbody-bag", "Leather Crossbody Bag", "Accessories", "Accessory", 68000, "TZS")
    );

    public StoreService(ObjectMapper objectMapper, @Value("${app.data-file}") String dataFile) {
        this.objectMapper = objectMapper;
        this.dataFile = Path.of(dataFile);
    }

    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "fadhil-backend",
                "timestamp", now()
        );
    }

    public List<Product> products() {
        return products;
    }

    public synchronized User register(Map<String, Object> body) {
        require(body, "name", "email", "password");
        Database database = readDatabase();
        String email = value(body, "email").toLowerCase();
        boolean exists = database.getUsers().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));

        if (exists) {
            throw new ConflictException("A user with this email already exists.");
        }

        UserRecord record = new UserRecord(
                UUID.randomUUID().toString(),
                value(body, "name"),
                email,
                hashPassword(value(body, "password")),
                now()
        );

        database.getUsers().add(record);
        writeDatabase(database);
        return publicUser(record);
    }

    public synchronized LoginResponse login(Map<String, Object> body) {
        require(body, "email", "password");
        Database database = readDatabase();
        String email = value(body, "email").toLowerCase();
        String passwordHash = hashPassword(value(body, "password"));

        UserRecord record = database.getUsers().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .filter(user -> user.getPasswordHash().equals(passwordHash))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        return new LoginResponse(publicUser(record), UUID.randomUUID().toString());
    }

    public synchronized OrderRequest createOrder(Map<String, Object> body) {
        String fullName = firstValue(body, "fullName", "name");
        String productRequest = firstValue(body, "productRequest", "product", "request");

        if (fullName.isBlank()) {
            throw new BadRequestException("Missing required fields: fullName");
        }

        if (productRequest.isBlank()) {
            throw new BadRequestException("Missing required fields: productRequest");
        }

        Database database = readDatabase();
        OrderRequest order = new OrderRequest(
                UUID.randomUUID().toString(),
                fullName,
                firstValue(body, "phoneOrEmail", "contact"),
                productRequest,
                value(body, "size"),
                firstValueOrDefault(body, "Pickup", "delivery"),
                value(body, "details"),
                "new",
                now()
        );

        database.getOrders().add(0, order);
        writeDatabase(database);
        return order;
    }

    public synchronized List<OrderRequest> orders() {
        return readDatabase().getOrders();
    }

    public synchronized ContactMessage createContactMessage(Map<String, Object> body) {
        require(body, "fullName", "phoneOrEmail", "message");
        Database database = readDatabase();
        ContactMessage message = new ContactMessage(
                UUID.randomUUID().toString(),
                value(body, "fullName"),
                value(body, "phoneOrEmail"),
                firstValueOrDefault(body, "Order an item", "topic"),
                value(body, "message"),
                "new",
                now()
        );

        database.getContacts().add(0, message);
        writeDatabase(database);
        return message;
    }

    public synchronized List<ContactMessage> contactMessages() {
        return readDatabase().getContacts();
    }

    private Database readDatabase() {
        try {
            ensureDatabaseFile();
            return objectMapper.readValue(dataFile.toFile(), Database.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read database file.", exception);
        }
    }

    private void writeDatabase(Database database) {
        try {
            ensureDatabaseFile();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), database);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write database file.", exception);
        }
    }

    private void ensureDatabaseFile() throws IOException {
        Path parent = dataFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        if (!Files.exists(dataFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), new Database());
        }
    }

    private void require(Map<String, Object> body, String... fields) {
        List<String> missing = new ArrayList<>();
        for (String field : fields) {
            if (value(body, field).isBlank()) {
                missing.add(field);
            }
        }

        if (!missing.isEmpty()) {
            throw new BadRequestException("Missing required fields: " + String.join(", ", missing));
        }
    }

    private User publicUser(UserRecord record) {
        return new User(record.getId(), record.getName(), record.getEmail(), record.getCreatedAt());
    }

    private String firstValue(Map<String, Object> body, String... fields) {
        for (String field : fields) {
            String value = value(body, field);
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String firstValueOrDefault(Map<String, Object> body, String defaultValue, String... fields) {
        String value = firstValue(body, fields);
        return value.isBlank() ? defaultValue : value;
    }

    private String value(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value instanceof String[] values && values.length > 0) {
            return values[0].trim();
        }
        if (value instanceof List<?> values && !values.isEmpty()) {
            return String.valueOf(values.get(0)).trim();
        }
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String now() {
        return Instant.now().toString();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}

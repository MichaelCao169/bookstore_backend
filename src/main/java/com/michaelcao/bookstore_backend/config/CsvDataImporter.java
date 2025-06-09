package com.michaelcao.bookstore_backend.config;

import com.michaelcao.bookstore_backend.entity.Category;
import com.michaelcao.bookstore_backend.entity.Product;
import com.michaelcao.bookstore_backend.repository.CategoryRepository;
import com.michaelcao.bookstore_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * CSV Data Importer - Imports book data from CSV file on application startup
 * 
 * To use this importer:
 * 1. Place your CSV file in src/main/resources/data/books.csv
 * 2. Make sure CSV has headers: title,authors,original_price,current_price,quantity,category,pages,publishler,cover_link
 * 3. Set app.import-csv-data=true in application.properties
 * 4. Run the application
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after DataInitializer
public class CsvDataImporter implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    // Control flag - set this to true in application.properties to enable import
    // app.import-csv-data=true
    private final boolean importEnabled = false; // Change to true to enable

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!importEnabled) {
            log.info("CSV data import is disabled. Set importEnabled=true to enable.");
            return;
        }

        log.info("Starting CSV data import...");
        
        try {
            // Check if products already exist
            long existingProductCount = productRepository.count();
            if (existingProductCount > 0) {
                log.info("Products already exist in database ({}). Skipping CSV import.", existingProductCount);
                return;
            }

            // Import categories first
            importCategories();
            
            // Import products from CSV
            importProductsFromCsv();
            
            log.info("CSV data import completed successfully!");
            
        } catch (Exception e) {
            log.error("Error during CSV data import: ", e);
        }
    }

    private void importCategories() {
        log.info("Creating default categories...");
        
        String[][] categories = {
            {"Văn học", "Sách văn học trong và ngoài nước"},
            {"Kinh doanh", "Sách về kinh doanh và quản lý"},
            {"Khoa học", "Sách khoa học và công nghệ"},
            {"Lịch sử", "Sách lịch sử và văn hóa"},
            {"Tâm lý", "Sách tâm lý học và phát triển bản thân"},
            {"Giáo dục", "Sách giáo dục và học tập"},
            {"Triết học", "Sách triết học và tư tưởng"},
            {"Nghệ thuật", "Sách về nghệ thuật và thẩm mỹ"}
        };

        for (String[] categoryData : categories) {
            String name = categoryData[0];
            String description = categoryData[1];
            
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                Category category = new Category(name, description);
                categoryRepository.save(category);
                log.debug("Created category: {}", name);
            }
        }
    }

    private void importProductsFromCsv() {
        log.info("Importing products from CSV...");
        
        try {
            ClassPathResource resource = new ClassPathResource("data/books.csv");
            if (!resource.exists()) {
                log.warn("CSV file not found at src/main/resources/data/books.csv");
                return;
            }

            // Cache categories for faster lookup
            Map<String, Category> categoryCache = createCategoryCache();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    log.warn("CSV file is empty");
                    return;
                }

                log.debug("CSV Headers: {}", headerLine);
                String[] headers = parseCSVLine(headerLine);
                
                String line;
                int lineNumber = 1;
                int successCount = 0;
                int errorCount = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    
                    try {
                        String[] values = parseCSVLine(line);
                        if (values.length < headers.length) {
                            log.warn("Line {}: Not enough columns, skipping", lineNumber);
                            errorCount++;
                            continue;
                        }

                        Product product = createProductFromCsvLine(headers, values, categoryCache);
                        if (product != null) {
                            productRepository.save(product);
                            successCount++;
                            
                            if (successCount % 10 == 0) {
                                log.info("Imported {} products...", successCount);
                            }
                        } else {
                            errorCount++;
                        }
                        
                    } catch (Exception e) {
                        log.error("Error processing line {}: {}", lineNumber, e.getMessage());
                        errorCount++;
                    }
                }

                log.info("CSV import completed: {} successful, {} errors", successCount, errorCount);
            }
            
        } catch (Exception e) {
            log.error("Error reading CSV file: ", e);
        }
    }

    private Map<String, Category> createCategoryCache() {
        Map<String, Category> cache = new HashMap<>();
        categoryRepository.findAll().forEach(category -> 
            cache.put(category.getName().toLowerCase(), category));
        return cache;
    }

    private Product createProductFromCsvLine(String[] headers, String[] values, Map<String, Category> categoryCache) {
        try {
            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < headers.length && i < values.length; i++) {
                row.put(headers[i].toLowerCase().trim(), values[i].trim());
            }

            // Extract fields
            String title = row.get("title");
            String author = row.get("authors");
            if (author == null || author.isEmpty()) {
                author = row.get("author"); // fallback
            }
            
            if (title == null || title.isEmpty() || author == null || author.isEmpty()) {
                log.warn("Skipping row: Missing title or author");
                return null;
            }

            // Parse prices
            BigDecimal originalPrice = parsePrice(row.get("original_price"));
            BigDecimal currentPrice = parsePrice(row.get("current_price"));
            
            if (originalPrice.compareTo(BigDecimal.ZERO) <= 0 || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Skipping row: Invalid prices for {}", title);
                return null;
            }

            // Parse numbers
            Integer quantity = parseInt(row.get("quantity"), 0);
            Integer pages = parseInt(row.get("pages"), 0);
            
            // Other fields
            String publisher = row.get("publishler"); // Note: typo in CSV header
            if (publisher == null || publisher.isEmpty()) {
                publisher = row.get("publisher"); // fallback
            }
            String coverLink = row.get("cover_link");
            String categoryName = row.get("category");

            // Find category
            Category category = findCategory(categoryName, categoryCache);

            // Create product
            Product product = new Product();
            product.setTitle(title);
            product.setAuthor(author);
            product.setOriginalPrice(originalPrice);
            product.setCurrentPrice(currentPrice);
            product.setQuantity(quantity);
            product.setPages(pages > 0 ? pages : null);
            product.setPublisher(publisher);
            product.setCoverLink(coverLink);
            product.setCategory(category);
            
            // Add category to categories set as well
            if (category != null) {
                product.getCategories().add(category);
            }

            return product;
            
        } catch (Exception e) {
            log.error("Error creating product from CSV line: ", e);
            return null;
        }
    }

    private Category findCategory(String categoryName, Map<String, Category> categoryCache) {
        if (categoryName == null || categoryName.isEmpty()) {
            return categoryCache.values().iterator().next(); // Return first category as default
        }

        String categoryLower = categoryName.toLowerCase().trim();
        
        // Exact match
        Category category = categoryCache.get(categoryLower);
        if (category != null) {
            return category;
        }

        // Partial match
        for (Map.Entry<String, Category> entry : categoryCache.entrySet()) {
            if (entry.getKey().contains(categoryLower) || categoryLower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default to first category
        return categoryCache.values().iterator().next();
    }

    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // Remove any non-digit characters except decimal point
            String cleaned = priceStr.replaceAll("[^\\d.]", "");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Invalid price format: {}", priceStr);
            return BigDecimal.ZERO;
        }
    }

    private Integer parseInt(String intStr, int defaultValue) {
        if (intStr == null || intStr.isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(intStr.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String[] parseCSVLine(String line) {
        // Simple CSV parser - handles quoted fields with commas
        if (line == null || line.isEmpty()) {
            return new String[0];
        }

        // For more complex CSV parsing, consider using OpenCSV library
        // This is a simple implementation
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
} 
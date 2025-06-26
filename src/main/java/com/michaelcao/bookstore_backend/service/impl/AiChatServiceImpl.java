package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.entity.Product;
import com.michaelcao.bookstore_backend.repository.ProductRepository;
import com.michaelcao.bookstore_backend.service.AiChatService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final ProductRepository productRepository;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private OpenAIClient openAIClient;

    @PostConstruct
    public void init() {
        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
            log.error("OpenAI API key is not configured. AI Chat functionality will not work.");
            throw new IllegalStateException("OpenAI API key must be configured");
        }
        
        this.openAIClient = OpenAIOkHttpClient.builder()
                .apiKey(openaiApiKey)
                .build();
        
        log.info("AI Chat Service initialized successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public String getAiResponse(String userQuery) {
        log.info("AI Chat: Processing query: '{}'", userQuery);

        // *** BƯỚC MỚI: TRÍCH XUẤT TỪ KHÓA BẰNG AI ***
        String searchKeywords = extractKeywordsFromQuery(userQuery);
        
        // Nếu không trích xuất được từ khóa, dùng câu gốc (fallback)
        if (searchKeywords.isEmpty()) {
            searchKeywords = userQuery;
            log.warn("Could not extract specific keywords, using original query for search.");
        }

        // 1. Enhanced Retrieval: Tìm kiếm các sản phẩm liên quan với keywords đã trích xuất
        List<Product> relevantProducts = findRelevantProducts(searchKeywords);
        log.info("AI Chat: Found {} relevant products for keywords '{}'", relevantProducts.size(), searchKeywords);
        
        // 2. Augmentation: Tạo ngữ cảnh từ dữ liệu sách
        String bookContext = buildBookContext(relevantProducts);

        // 3. Generation: Tạo prompt hoàn chỉnh và gọi API của OpenAI
        String systemPrompt = "Bạn là một trợ lý ảo am hiểu về sách tên là Atom, làm việc tại hiệu sách AtomikBooks. " +
                "Nhiệm vụ của bạn là tư vấn sách cho khách hàng một cách thân thiện, nhiệt tình và chuyên nghiệp. " +
                "Dựa vào danh sách các cuốn sách được cung cấp dưới đây, hãy trả lời câu hỏi của khách hàng. " +
                "Nếu có sách liên quan trực tiếp, hãy ưu tiên giới thiệu những cuốn sách đó. " +
                "Nếu không có sách liên quan trực tiếp, hãy giới thiệu những cuốn sách hay có sẵn trong cửa hàng. " +
                "Luôn luôn cố gắng giới thiệu ít nhất một cuốn sách từ danh sách có sẵn. " +
                "Bao gồm thông tin về giá cả và khuyến khích khách hàng mua hàng. " +
                "Câu trả lời cần ngắn gọn, tập trung vào việc giới thiệu sách.";

        String userPrompt = "Dưới đây là danh sách các cuốn sách có thể liên quan:\n" +
                "---BEGIN BOOK LIST---\n" +
                bookContext +
                "\n---END BOOK LIST---\n\n" +
                "Dựa vào danh sách trên, hãy trả lời câu hỏi của khách hàng: \"" + userQuery + "\"";

        try {
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model("gpt-4o-mini")
                    .addSystemMessage(systemPrompt)
                    .addUserMessage(userPrompt)
                    .maxTokens(300)
                    .temperature(0.5)
                    .build();

            ChatCompletion response = openAIClient.chat().completions().create(request);

            if (response != null && !response.choices().isEmpty()) {
                String aiReply = response.choices().get(0).message().content().orElse("");
                log.info("AI Chat: Successfully generated response");
                return aiReply;
            } else {
                log.warn("AI Chat: OpenAI response was empty");
                return "Xin lỗi, tôi không thể tạo câu trả lời lúc này.";
            }

        } catch (Exception e) {
            log.error("AI Chat: Error calling OpenAI API", e);
            return "Xin lỗi, tôi đang gặp một sự cố kỹ thuật. Vui lòng thử lại sau.";
        }
    }

    /**
     * Trích xuất từ khóa quan trọng từ câu hỏi của người dùng bằng OpenAI
     */
    private String extractKeywordsFromQuery(String userQuery) {
        // Một prompt chuyên để trích xuất thông tin
        String extractionPrompt = String.format(
            "Phân tích câu hỏi sau và trích xuất TÊN TÁC GIẢ hoặc TÊN SÁCH cụ thể để tìm kiếm trong cơ sở dữ liệu. " +
            "\n\nCÁCH TRÍCH XUẤT:" +
            "\n- Nếu có tên tác giả (VD: Nguyễn Nhật Ánh, Nguyễn Nhật Anh): trả về chính xác tên tác giả" +
            "\n- Nếu có tên sách (VD: Tôi thấy hoa vàng trên cỏ xanh): trả về chính xác tên sách" +
            "\n- Nếu có cả hai: ưu tiên tên tác giả" +
            "\n- Nếu không có tên cụ thể: trích xuất từ khóa chính (VD: 'tiểu thuyết', 'truyện ngắn')" +
            "\n\nQUAN TRỌNG: CHỈ trả về kết quả trích xuất, KHÔNG giải thích hay thêm từ gì khác." +
            "\n\nCâu hỏi: \"%s\"" +
            "\nKết quả trích xuất:", userQuery
        );

        try {
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model("gpt-4o-mini")
                    .addSystemMessage("Bạn là chuyên gia trích xuất thông tin. Chỉ trả về kết quả trích xuất, không giải thích.")
                    .addUserMessage(extractionPrompt)
                    .maxTokens(50) // Giới hạn token vì chỉ cần lấy tên/từ khóa
                    .temperature(0.0) // Temperature = 0 để kết quả nhất quán
                    .build();

            ChatCompletion response = openAIClient.chat().completions().create(request);

            if (response != null && !response.choices().isEmpty()) {
                String extractedKeywords = response.choices().get(0).message().content().orElse("").trim();
                log.info("🔍 AI Extraction - Input: '{}' => Output: '{}'", userQuery, extractedKeywords);
                // Xóa dấu nháy kép và ký tự thừa nếu có
                return extractedKeywords.replace("\"", "").replace("'", "").trim();
            }
        } catch (Exception e) {
            log.error("❌ Error extracting keywords from query: '{}'", userQuery, e);
        }
        return ""; // Trả về chuỗi rỗng nếu có lỗi
    }

    /**
     * Enhanced search method with multiple strategies
     */
    private List<Product> findRelevantProducts(String userQuery) {
        String normalizedQuery = normalizeVietnameseText(userQuery);
        log.info("AI Chat: Original query: '{}', Normalized: '{}'", userQuery, normalizedQuery);
        
        // Extract important keywords from the query
        List<String> keywords = extractKeywords(normalizedQuery);
        log.info("AI Chat: Extracted keywords: {}", keywords);
        
        // Strategy 1: Exact keyword search in title or author (with eager category loading)
        List<Product> exactMatches = productRepository.searchByTitleOrAuthorWithCategory(normalizedQuery, PageRequest.of(0, 5)).getContent();
        
        if (!exactMatches.isEmpty()) {
            log.info("AI Chat: Found {} exact matches", exactMatches.size());
            return exactMatches;
        }
        
        // Strategy 2: Try original query without normalization (in case user uses English)
        if (!userQuery.equals(normalizedQuery)) {
            List<Product> originalMatches = productRepository.searchByTitleOrAuthorWithCategory(userQuery, PageRequest.of(0, 5)).getContent();
            if (!originalMatches.isEmpty()) {
                log.info("AI Chat: Found {} matches with original query", originalMatches.size());
                return originalMatches;
            }
        }
        
        // Strategy 3: Try with extracted keywords
        for (String keyword : keywords) {
            if (keyword.length() > 1) { // Allow shorter keywords for book titles
                List<Product> keywordMatches = productRepository.searchByTitleOrAuthorWithCategory(keyword, PageRequest.of(0, 5)).getContent();
                if (!keywordMatches.isEmpty()) {
                    log.info("AI Chat: Found {} matches for keyword '{}'", keywordMatches.size(), keyword);
                    return keywordMatches;
                }
            }
        }
        
        // Strategy 4: Try with individual words from the normalized query
        String[] queryWords = normalizedQuery.split("\\s+");
        for (String word : queryWords) {
            if (word.length() > 2) { // Skip very short words
                List<Product> wordMatches = productRepository.searchByTitleOrAuthorWithCategory(word, PageRequest.of(0, 5)).getContent();
                if (!wordMatches.isEmpty()) {
                    log.info("AI Chat: Found {} matches for word '{}'", wordMatches.size(), word);
                    return wordMatches;
                }
            }
        }
        
        // Strategy 5: Try title-only search (with eager category loading)
        List<Product> titleMatches = productRepository.findByTitleContainingIgnoreCaseWithCategory(normalizedQuery, PageRequest.of(0, 5)).getContent();
        if (!titleMatches.isEmpty()) {
            log.info("AI Chat: Found {} title matches", titleMatches.size());
            return titleMatches;
        }
        
        // Strategy 6: Try author-only search (with eager category loading)
        List<Product> authorMatches = productRepository.findByAuthorContainingIgnoreCaseWithCategory(normalizedQuery, PageRequest.of(0, 5)).getContent();
        if (!authorMatches.isEmpty()) {
            log.info("AI Chat: Found {} author matches", authorMatches.size());
            return authorMatches;
        }
        
        // Strategy 7: Try partial matches for each keyword
        for (String keyword : keywords) {
            if (keyword.length() > 1) {
                List<Product> partialTitleMatches = productRepository.findByTitleContainingIgnoreCaseWithCategory(keyword, PageRequest.of(0, 3)).getContent();
                if (!partialTitleMatches.isEmpty()) {
                    log.info("AI Chat: Found {} partial title matches for keyword '{}'", partialTitleMatches.size(), keyword);
                    return partialTitleMatches;
                }
                
                List<Product> partialAuthorMatches = productRepository.findByAuthorContainingIgnoreCaseWithCategory(keyword, PageRequest.of(0, 3)).getContent();
                if (!partialAuthorMatches.isEmpty()) {
                    log.info("AI Chat: Found {} partial author matches for keyword '{}'", partialAuthorMatches.size(), keyword);
                    return partialAuthorMatches;
                }
            }
        }
        
        // Strategy 8: Try partial matches for each word
        for (String word : queryWords) {
            if (word.length() > 2) {
                List<Product> partialTitleMatches = productRepository.findByTitleContainingIgnoreCaseWithCategory(word, PageRequest.of(0, 3)).getContent();
                if (!partialTitleMatches.isEmpty()) {
                    log.info("AI Chat: Found {} partial title matches for word '{}'", partialTitleMatches.size(), word);
                    return partialTitleMatches;
                }
                
                List<Product> partialAuthorMatches = productRepository.findByAuthorContainingIgnoreCaseWithCategory(word, PageRequest.of(0, 3)).getContent();
                if (!partialAuthorMatches.isEmpty()) {
                    log.info("AI Chat: Found {} partial author matches for word '{}'", partialAuthorMatches.size(), word);
                    return partialAuthorMatches;
                }
            }
        }
        
        // Strategy 9: Fallback to popular books (best sellers) with eager category loading
        List<Product> fallbackProducts = productRepository.findAllByOrderBySoldCountDescWithCategory();
        if (!fallbackProducts.isEmpty()) {
            log.info("AI Chat: Using {} fallback products (best sellers)", Math.min(fallbackProducts.size(), 3));
            return fallbackProducts.subList(0, Math.min(fallbackProducts.size(), 3));
        }
        
        // Strategy 10: Last resort - any available books with eager category loading
        List<Product> anyProducts = productRepository.findAllWithCategoriesFetched(PageRequest.of(0, 3)).getContent();
        log.info("AI Chat: Using {} random products as last resort", anyProducts.size());
        return anyProducts;
    }

    /**
     * Extract meaningful keywords from user query by removing noise words
     */
    private List<String> extractKeywords(String query) {
        // Common Vietnamese noise words to filter out
        String[] noiseWords = {
            "co", "có", "cuon", "cuốn", "sach", "sách", "nao", "nào", "gi", "gì", 
            "the", "thế", "la", "là", "cua", "của", "trong", "va", "và", "voi", "với",
            "cho", "toi", "tôi", "moi", "mình", "ban", "bạn", "anh", "chi", "chị",
            "khong", "không", "tim", "tìm", "kiem", "kiếm", "hay", "dep", "đẹp",
            "tot", "tốt", "nhat", "nhất", "ve", "về", "roi", "rồi", "da", "đã", "se", "sẽ",
            "duoc", "được", "dang", "đang", "cang", "càng", "rat", "rất", "qua", "quá"
        };
        
        // Use HashSet to handle potential duplicates in noiseWords array
        Set<String> noiseSet = new HashSet<>(Arrays.asList(noiseWords));
        
        return Arrays.stream(query.split("\\s+"))
                .map(String::trim)
                .filter(word -> word.length() > 1)
                .filter(word -> !noiseSet.contains(word.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Normalize Vietnamese text by removing diacritics and converting to lowercase
     */
    private String normalizeVietnameseText(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        
        // Convert to lowercase and trim
        String normalized = text.toLowerCase().trim();
        
        // Remove extra spaces and normalize whitespace
        normalized = normalized.replaceAll("\\s+", " ");
        
        // Remove common Vietnamese diacritics
        normalized = normalized
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "a")
                .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "e")
                .replaceAll("[ÌÍỊỈĨ]", "i")
                .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "o")
                .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "u")
                .replaceAll("[ỲÝỴỶỸ]", "y")
                .replaceAll("Đ", "d");
        
        // Remove punctuation marks that might interfere with search
        normalized = normalized.replaceAll("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]", " ");
        
        // Remove extra spaces again after punctuation removal
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        return normalized;
    }

    private String buildBookContext(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "Không có sách nào trong danh sách.";
        }

        return products.stream()
                .map(product -> {
                    StringBuilder bookInfo = new StringBuilder();
                    
                    // Null-safe title
                    String title = product.getTitle() != null ? product.getTitle() : "Tên sách không xác định";
                    bookInfo.append(String.format("📚 **%s**", title));
                    
                    // Null-safe author
                    String author = product.getAuthor() != null ? product.getAuthor() : "Tác giả không xác định";
                    bookInfo.append(String.format("\n   ✍️ Tác giả: %s", author));
                    
                    if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
                        String description = product.getDescription().length() > 200
                                ? product.getDescription().substring(0, 200) + "..."
                                : product.getDescription();
                        bookInfo.append(String.format("\n   📖 Mô tả: %s", description));
                    }
                    
                    // Null-safe price
                    if (product.getCurrentPrice() != null) {
                        bookInfo.append(String.format("\n   💰 Giá: %,.0f VND", product.getCurrentPrice()));
                    } else {
                        bookInfo.append("\n   💰 Giá: Liên hệ");
                    }
                    
                    // Fix NullPointerException: kiểm tra null trước khi so sánh
                    if (product.getSoldCount() != null && product.getSoldCount() > 0) {
                        bookInfo.append(String.format("\n   📊 Đã bán: %d cuốn", product.getSoldCount()));
                    }
                    
                    // Fix NullPointerException: kiểm tra null cho category
                    if (product.getCategory() != null && product.getCategory().getName() != null) {
                        bookInfo.append(String.format("\n   🏷️ Thể loại: %s", product.getCategory().getName()));
                    }
                    
                    return bookInfo.toString();
                })
                .collect(Collectors.joining("\n\n"));
    }
}
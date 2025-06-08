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

import java.util.List;
import java.util.stream.Collectors;

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
    public String getAiResponse(String userQuery) {
        log.info("AI Chat: Processing query from user");

        // 1. Retrieval: Tìm kiếm các sản phẩm liên quan trong DB
        List<Product> relevantProducts = productRepository.searchByTitleOrAuthor(userQuery, PageRequest.of(0, 5)).getContent();
        log.info("AI Chat: Found {} relevant products", relevantProducts.size());
        
        // Nếu không tìm thấy sản phẩm liên quan, lấy một vài sản phẩm ngẫu nhiên
        if (relevantProducts.isEmpty()) {
            long totalProducts = productRepository.count();
            if (totalProducts > 0) {
                log.info("AI Chat: No specific matches found, using fallback products");
                relevantProducts = productRepository.findAll(PageRequest.of(0, 3)).getContent();
            } else {
                log.warn("AI Chat: No products available in database");
            }
        }

        // 2. Augmentation: Tạo ngữ cảnh từ dữ liệu sách
        String bookContext = buildBookContext(relevantProducts);

        // 3. Generation: Tạo prompt hoàn chỉnh và gọi API của OpenAI
        String systemPrompt = "Bạn là một trợ lý ảo am hiểu về sách tên là Atom, làm việc tại hiệu sách AtomikBooks. " +
                "Nhiệm vụ của bạn là tư vấn sách cho khách hàng một cách thân thiện, nhiệt tình và chuyên nghiệp. " +
                "Dựa vào danh sách các cuốn sách được cung cấp dưới đây, hãy trả lời câu hỏi của khách hàng. " +
                "Nếu không có sách liên quan trực tiếp đến câu hỏi, hãy giới thiệu những cuốn sách hay có sẵn trong cửa hàng. " +
                "Luôn luôn cố gắng giới thiệu ít nhất một cuốn sách từ danh sách có sẵn. " +
                "Câu trả lời cần ngắn gọn, tập trung vào việc giới thiệu sách và khuyến khích khách hàng mua hàng.";

        // Kiểm tra xem có phải dùng fallback products không
        boolean isUsingFallback = relevantProducts.size() > 0 && 
                                  productRepository.searchByTitleOrAuthor(userQuery, PageRequest.of(0, 1)).isEmpty();

        String userPrompt;
        if (isUsingFallback) {
            userPrompt = "Dưới đây là một số cuốn sách hay có sẵn tại cửa hàng:\n" +
                    "---BEGIN BOOK LIST---\n" +
                    bookContext +
                    "\n---END BOOK LIST---\n\n" +
                    "Khách hàng hỏi: \"" + userQuery + "\"\n" +
                    "Hãy trả lời thân thiện và giới thiệu những cuốn sách hay từ danh sách trên, " +
                    "ngay cả khi chúng không liên quan trực tiếp đến câu hỏi.";
        } else {
            userPrompt = "Dưới đây là danh sách các cuốn sách có thể liên quan:\n" +
                    "---BEGIN BOOK LIST---\n" +
                    bookContext +
                    "\n---END BOOK LIST---\n\n" +
                    "Dựa vào danh sách trên, hãy trả lời câu hỏi của khách hàng: \"" + userQuery + "\"";
        }

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

    private String buildBookContext(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "Không có sách nào trong danh sách.";
        }

        return products.stream()
                .map(product -> String.format(
                        "- Tiêu đề: %s\n  Tác giả: %s\n  Mô tả: %s\n  Giá: %,.0f VND",
                        product.getTitle(),
                        product.getAuthor(),
                        product.getDescription() != null && product.getDescription().length() > 150
                                ? product.getDescription().substring(0, 150) + "..."
                                : product.getDescription(),
                        product.getCurrentPrice()
                ))
                .collect(Collectors.joining("\n\n"));
    }
}
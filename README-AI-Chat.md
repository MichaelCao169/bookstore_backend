# AI Chat Feature Documentation

## Overview
The AI Chat feature allows customers to interact with an AI assistant named "Atom" who can help recommend books based on the store's inventory. The AI uses OpenAI's GPT-4o-mini model to provide intelligent book recommendations.

## Features
- **Intelligent Book Search**: AI searches through the product database based on user queries
- **Fallback Recommendations**: If no specific matches are found, AI recommends available books from the store
- **Natural Language Interaction**: Users can ask questions in natural Vietnamese language
- **Context-Aware Responses**: AI provides responses based on actual store inventory

## API Endpoint

### Send Chat Message
```
POST /api/ai-chat/send
Content-Type: application/json
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "message": "Tôi muốn tìm sách về lập trình"
}
```

**Response:**
```json
{
  "reply": "Chào bạn! Dựa vào tìm kiếm của tôi, hiện tại cửa hàng có một số cuốn sách lập trình hay như..."
}
```

## Security
- Requires authentication (JWT token)
- Only authenticated users can access the AI chat feature
- Input validation and sanitization applied

## How It Works

1. **Query Processing**: User's message is received and validated
2. **Product Search**: AI service searches for relevant products using title and author fields
3. **Context Building**: Found products are formatted into a context for the AI
4. **AI Generation**: OpenAI API generates a response based on the context and user query
5. **Fallback Mechanism**: If no specific products match, AI recommends from available inventory

## Configuration

### Required Environment Variables
- `OPENAI_API_KEY`: Your OpenAI API key (required)

### Error Handling
- If OpenAI API is unavailable: Returns friendly error message
- If no products in database: AI informs user that store is being updated
- If API key is missing: Service fails to start with clear error message

## Example Conversations

**User:** "Tôi muốn tìm sách về lịch sử Việt Nam"
**AI:** "Chào bạn! Tôi tìm thấy một số cuốn sách về lịch sử Việt Nam rất hay..."

**User:** "Có sách nào hay không?"
**AI:** "Chào bạn! Hiện tại cửa hàng có nhiều cuốn sách hay. Tôi xin giới thiệu một số cuốn..."

## Monitoring and Logging
- All chat requests are logged for monitoring
- Error tracking for API failures
- Performance metrics for response times

## Production Considerations
- API rate limiting should be implemented at load balancer level
- Consider caching frequently asked questions
- Monitor OpenAI API usage and costs
- Regular monitoring of response quality 
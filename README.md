# Bookstore Backend

## Yêu cầu hệ thống
- Java 17+
- MySQL 8.0+
- Maven 3.6+

## Cài đặt và chạy ứng dụng

### 1. Clone repository
```bash
git clone <repository-url>
cd bookstore-backend
```

### 2. Cấu hình cơ sở dữ liệu
- Tạo database MySQL với tên `bookstore_db`
- Cập nhật thông tin kết nối trong `application.properties` nếu cần

### 3. Cấu hình biến môi trường
Tạo file `.env` trong thư mục root với nội dung:
```properties
OPENAI_API_KEY=your-openai-api-key-here
```

**Lưu ý**: File `.env` đã được thêm vào `.gitignore` để bảo mật API key.

### 4. Chạy ứng dụng
```bash
# Sử dụng Maven Wrapper
./mvnw spring-boot:run

# Hoặc với Maven
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại `http://localhost:8080`

## Cấu hình quan trọng

### Bảo mật
- JWT Secret được cấu hình trong `application.properties`
- OpenAI API Key được đọc từ biến môi trường `OPENAI_API_KEY`
- Thông tin email được cấu hình trong `application.properties` (nên sử dụng App Password cho Gmail)

### Upload files
- Thư mục upload: `uploads/`
- Kích thước file tối đa: 10MB

## API Documentation
API sẽ có sẵn tại `http://localhost:8080` sau khi ứng dụng được khởi động.

## Lưu ý bảo mật
- Không commit API keys hoặc thông tin nhạy cảm vào Git
- Sử dụng biến môi trường cho production
- Thay đổi JWT secret trong production

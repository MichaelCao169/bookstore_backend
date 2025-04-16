package com.michaelcao.bookstore_backend.dto.order;

import com.michaelcao.bookstore_backend.entity.OrderStatus; // Import Enum
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "New status cannot be null")
    private OrderStatus status; // Admin sẽ gửi lên trạng thái mới (ví dụ: PROCESSING, SHIPPED, ...)
}
package com.michaelcao.bookstore_backend.dto.category;

import lombok.Data;
import lombok.NoArgsConstructor; 

@Data 
@NoArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;

    // Constructor với tham số (hữu ích khi mapping)
    public CategoryDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
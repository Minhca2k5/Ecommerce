package com.minzetsu.ecommerce.search.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLogRequest {

    private Long userId; // nullable

    @NotBlank(message = "Keyword is required")
    private String keyword;
}

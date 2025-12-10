package com.minzetsu.ecommerce.search.service;

import com.minzetsu.ecommerce.search.dto.request.SearchLogRequest;
import com.minzetsu.ecommerce.search.dto.response.SearchLogResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchLogService {
    SearchLogResponse addSearchLog(SearchLogRequest request, Long userId);
    List<SearchLogResponse> getSearchLogsByKeyword(String keyword, Long userId);
    List<SearchLogResponse> getSearchLogsByUserId(Long userId, Pageable pageable);
    void deleteAllSearchLogsByUserId(Long userId);
    void deleteSearchLog(Long id, Long userId);
}

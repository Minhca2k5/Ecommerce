package com.minzetsu.ecommerce.activity.service;

import com.minzetsu.ecommerce.activity.dto.request.RecentViewRequest;
import com.minzetsu.ecommerce.activity.dto.response.RecentViewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecentViewService {
    Page<RecentViewResponse> getRecentViewsByUserId(Long userId, Pageable pageable);
    void deleteRecentView(Long id, Long userId);
    RecentViewResponse addRecentView(RecentViewRequest request, Long userId);
    void deleteAllRecentViewsByUserId(Long userId);
    List<RecentViewResponse> getRecentViewsByProductName(String productName, Long userId);
}

package com.minzetsu.ecommerce.search.service.impl;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.search.dto.request.SearchLogRequest;
import com.minzetsu.ecommerce.search.dto.response.SearchLogResponse;
import com.minzetsu.ecommerce.search.entity.SearchLog;
import com.minzetsu.ecommerce.search.mapper.SearchLogMapper;
import com.minzetsu.ecommerce.search.repository.SearchLogRepository;
import com.minzetsu.ecommerce.search.service.SearchLogService;
import com.minzetsu.ecommerce.user.service.UserService;
import com.minzetsu.ecommerce.mongo.ClickstreamEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchLogServiceImpl implements SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final SearchLogMapper searchLogMapper;
    private final UserService userService;
    private final ClickstreamEventService clickstreamEventService;

    @Override
    @Transactional
    public SearchLogResponse addSearchLog(SearchLogRequest request, Long userId) {
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        String keyword = request.getKeyword();
        clickstreamEventService.recordSearch(userId, keyword);
        Optional<SearchLog> existingLog = searchLogRepository.findByUserIdAndKeywordIgnoreCase(userId, keyword);
        if (existingLog.isPresent()) {
            SearchLog log = existingLog.get();
            log.setKeyword(log.getKeyword());
            return searchLogMapper.toResponse(searchLogRepository.save(log));
        }
        SearchLog newLog = searchLogMapper.toEntity(request);
        newLog.setUser(userService.getUserById(userId));
        return searchLogMapper.toResponse(searchLogRepository.save(newLog));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchLogResponse> getSearchLogsByKeyword(String keyword, Long userId) {
        List<SearchLog> searchLogs = searchLogRepository.findByUserIdAndKeywordContainingIgnoreCase(userId, keyword);
        return searchLogMapper.toResponseList(searchLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchLogResponse> getSearchLogsByUserId(Long userId, Pageable pageable) {
        List<SearchLog> searchLogs = searchLogRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
        return searchLogMapper.toResponseList(searchLogs);
    }

    @Override
    @Transactional
    public void deleteAllSearchLogsByUserId(Long userId) {
        searchLogRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteSearchLog(Long id, Long userId) {
        SearchLog searchLog = searchLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Search log with ID " + id + " not found."));
        if (!searchLog.getUser().getId().equals(userId)) {
            throw new NotFoundException("User with ID " + userId + " is not authorized to delete this search log.");
        }
        searchLogRepository.deleteById(id);
    }
}

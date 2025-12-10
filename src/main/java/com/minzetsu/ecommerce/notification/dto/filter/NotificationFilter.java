package com.minzetsu.ecommerce.notification.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationFilter implements SortableFilter {
    private Boolean isRead;
    private Boolean isHidden;
    private List<String> types;
    private String referenceType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String sortBy;
    private String sortDirection;
}

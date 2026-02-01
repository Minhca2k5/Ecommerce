package com.minzetsu.ecommerce.search.controller.admin;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.search.service.ProductIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/search")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Search", description = "Admin search indexing actions")
@RequiredArgsConstructor
public class AdminSearchController {
    private final ProductIndexService productIndexService;

    

    @Operation(summary = "Reindex all products")
    @PostMapping("/reindex")
    public void reindexAll() {
        productIndexService.reindexAll();
    }
}

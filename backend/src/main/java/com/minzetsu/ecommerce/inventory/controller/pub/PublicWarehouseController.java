package com.minzetsu.ecommerce.inventory.controller.pub;

import com.minzetsu.ecommerce.inventory.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/warehouses")
@RequiredArgsConstructor
@Tag(name = "Public - Warehouses", description = "Public warehouse data for storefront filters")
public class PublicWarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "Get available warehouse locations", description = "Returns distinct active warehouse city names for product filtering.")
    @GetMapping("/locations")
    public ResponseEntity<List<String>> getWarehouseLocations() {
        return ResponseEntity.ok(warehouseService.getPublicWarehouseLocations());
    }
}

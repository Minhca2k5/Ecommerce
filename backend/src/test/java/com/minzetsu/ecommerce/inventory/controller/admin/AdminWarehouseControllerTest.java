package com.minzetsu.ecommerce.inventory.controller.admin;

import com.minzetsu.ecommerce.inventory.dto.request.WarehouseCreateRequest;
import com.minzetsu.ecommerce.inventory.dto.request.WarehouseUpdateRequest;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.inventory.dto.response.WarehouseResponse;
import com.minzetsu.ecommerce.inventory.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminWarehouseControllerTest {

    @Mock
    private WarehouseService warehouseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminWarehouseController controller = new AdminWarehouseController(warehouseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createWarehouse_shouldReturnCreatedWarehouse() throws Exception {
        when(warehouseService.createWarehouseResponse(any(WarehouseCreateRequest.class)))
                .thenReturn(WarehouseResponse.builder()
                        .id(11L)
                        .code("WH-11")
                        .name("North Hub")
                        .city("Hanoi")
                        .country("VN")
                        .isActive(true)
                        .build());

        mockMvc.perform(post("/api/admin/warehouses")
                .contentType("application/json")
                .content("""
                        {
                          "code": "WH-11",
                          "name": "North Hub",
                          "city": "Hanoi",
                          "country": "VN"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.code").value("WH-11"))
                .andExpect(jsonPath("$.name").value("North Hub"))
                .andExpect(jsonPath("$.city").value("Hanoi"))
                .andExpect(jsonPath("$.country").value("VN"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void updateWarehouseById_shouldReturnUpdatedWarehouse() throws Exception {
        when(warehouseService.updateWarehouseResponse(any(WarehouseUpdateRequest.class), eq(11L)))
                .thenReturn(WarehouseResponse.builder()
                        .id(11L)
                        .code("WH-11-NEW")
                        .name("North Hub Updated")
                        .city("Hanoi")
                        .country("VN")
                        .isActive(false)
                        .build());

        mockMvc.perform(put("/api/admin/warehouses/11")
                .contentType("application/json")
                .content("""
                        {
                          "code": "WH-11-NEW",
                          "name": "North Hub Updated",
                          "city": "Hanoi",
                          "country": "VN",
                          "isActive": false
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.code").value("WH-11-NEW"))
                .andExpect(jsonPath("$.name").value("North Hub Updated"))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(warehouseService).updateWarehouseResponse(any(WarehouseUpdateRequest.class), eq(11L));
    }

    @Test
    void updateWarehouseStatus_shouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/admin/warehouses/11/status")
                .param("active", "false"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(warehouseService).updateIsActiveAndId(false, 11L);
    }

    @Test
    void getWarehouseById_shouldReturnWarehouse() throws Exception {
        when(warehouseService.getWarehouseResponseById(11L))
                .thenReturn(WarehouseResponse.builder()
                        .id(11L)
                        .code("WH-11")
                        .name("North Hub")
                        .city("Hanoi")
                        .country("VN")
                        .isActive(true)
                        .build());

        mockMvc.perform(get("/api/admin/warehouses/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.code").value("WH-11"))
                .andExpect(jsonPath("$.name").value("North Hub"));
    }

    @Test
    void getWarehouseDetailsById_shouldReturnWarehouseWithInventories() throws Exception {
        when(warehouseService.getFullWarehouseResponseById(11L))
                .thenReturn(WarehouseResponse.builder()
                        .id(11L)
                        .code("WH-11")
                        .name("North Hub")
                        .city("Hanoi")
                        .country("VN")
                        .inventories(List.of(InventoryResponse.builder()
                                .id(99L)
                                .productId(7L)
                                .productName("Phone X")
                                .warehouseId(11L)
                                .stockQty(25)
                                .reservedQty(2)
                                .isActive(true)
                                .build()))
                        .build());

        mockMvc.perform(get("/api/admin/warehouses/11/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.inventories[0].id").value(99))
                .andExpect(jsonPath("$.inventories[0].productName").value("Phone X"))
                .andExpect(jsonPath("$.inventories[0].warehouseId").value(11))
                .andExpect(jsonPath("$.inventories[0].stockQty").value(25));
    }

    @Test
    void searchWarehouses_shouldReturnPageContent() throws Exception {
        Page<WarehouseResponse> page = new PageImpl<>(List.of(
                WarehouseResponse.builder()
                        .id(11L)
                        .code("WH-11")
                        .name("North Hub")
                        .city("Hanoi")
                        .country("VN")
                        .isActive(true)
                        .build()),
                PageRequest.of(0, 10), 1);

        when(warehouseService.searchWarehouseResponses(any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/warehouses")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11))
                .andExpect(jsonPath("$.content[0].code").value("WH-11"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deleteWarehouseById_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/warehouses/11"))
                .andExpect(status().isNoContent());

        verify(warehouseService).deleteWarehouse(11L);
    }
}

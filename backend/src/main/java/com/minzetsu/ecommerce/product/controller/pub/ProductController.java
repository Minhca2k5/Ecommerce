package com.minzetsu.ecommerce.product.controller.pub;

import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.dto.response.FlashSaleResponse;
import com.minzetsu.ecommerce.product.dto.response.ProductResponse;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import com.minzetsu.ecommerce.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Xem và tìm kiếm sản phẩm")
public class ProductController {

        private final ProductService productService;
        private final ReviewService reviewService;
        @Value("${days}")
        private Integer days;
        @Value("${limit}")
        private Integer limit;

        @Operation(summary = "Tìm kiếm sản phẩm", description = "Lọc và tìm kiếm các sản phẩm theo tên, danh mục, trạng thái hoặc khoảng giá. Hỗ trợ phân trang và sắp xếp.")
        @GetMapping
        public ResponseEntity<Page<ProductResponse>> searchProducts(
                        @ModelAttribute ProductFilter filter,
                        Pageable pageable) {
                return ResponseEntity.ok(productService.searchProductResponses(filter, pageable));
        }

        @Operation(summary = "Lấy thông tin chi tiết sản phẩm theo ID", description = "Trả về thông tin chi tiết của sản phẩm, bao gồm hình ảnh, danh mục, tồn kho và thuộc tính khác.")
        @GetMapping("/{productId}")
        public ResponseEntity<ProductResponse> getProductById(
                        @PathVariable("productId") Long productId) {
                return ResponseEntity.ok(productService.getFullProductResponseById(productId));
        }

        @Operation(summary = "Lấy thông tin chi tiết sản phẩm theo slug", description = "Trả về thông tin chi tiết của sản phẩm theo slug, bao gồm hình ảnh, danh mục, tồn kho và các thuộc tính khác.")
        @GetMapping("/slug/{slug}")
        public ResponseEntity<ProductResponse> getProductBySlug(
                        @PathVariable("slug") String slug) {
                return ResponseEntity.ok(productService.getFullProductResponseBySlug(slug));
        }

        @Operation(summary = "Xem danh sách đánh giá của sản phẩm", description = "Trả về tất cả các đánh giá của một sản phẩm cụ thể (hiển thị công khai cho mọi người dùng).")
        @GetMapping("/{productId}/reviews")
        public ResponseEntity<List<ReviewResponse>> getReviewsByProductId(
                        @PathVariable("productId") Long productId) {
                return ResponseEntity.ok(reviewService.getReviewResponseByProductId(productId));
        }

        @Operation(summary = "Lấy danh sách sản phẩm đánh giá cao", description = "Trả về danh sách các sản phẩm có đánh giá cao nhất trong một khoảng thời gian nhất định.")
        @ApiResponse(responseCode = "200", description = "Danh sách sản phẩm đánh giá cao được trả về thành công")
        @GetMapping("/top-rating")
        public ResponseEntity<List<ProductResponse>> getTopRatingProducts() {
                return ResponseEntity.ok(productService.getTopRatingProductResponses(days, limit));
        }

        @Operation(summary = "Lấy danh sách sản phẩm được yêu thích nhiều nhất", description = "Trả về danh sách các sản phẩm được thêm vào danh sách yêu thích nhiều nhất trong một khoảng thời gian nhất định.")
        @ApiResponse(responseCode = "200", description = "Danh sách sản phẩm được yêu thích nhiều nhất được trả về thành công")
        @GetMapping("/most-favorite")
        public ResponseEntity<List<ProductResponse>> getMostFavoriteProducts() {
                return ResponseEntity.ok(productService.getMostFavoriteProductResponses(days, limit));
        }

        @Operation(summary = "Lấy danh sách sản phẩm được xem nhiều nhất", description = "Trả về danh sách các sản phẩm được xem nhiều nhất trong một khoảng thời gian nhất định.")
        @ApiResponse(responseCode = "200", description = "Danh sách sản phẩm được xem nhiều nhất được trả về thành công")
        @GetMapping("/most-viewed")
        public ResponseEntity<List<ProductResponse>> getMostViewedProducts() {
                return ResponseEntity.ok(productService.getMostViewedProductResponses(days, limit));
        }

        @Operation(summary = "Lấy danh sách sản phẩm bán chạy nhất", description = "Trả về danh sách các sản phẩm bán chạy nhất trong một khoảng thời gian nhất định.")
        @ApiResponse(responseCode = "200", description = "Danh sách sản phẩm bán chạy nhất được trả về thành công")
        @GetMapping("/best-selling")
        public ResponseEntity<List<ProductResponse>> getBestSellingProducts() {
                return ResponseEntity.ok(productService.getBestSellingProductResponses(days, limit));
        }

        @Operation(summary = "Lấy dữ liệu flash sale", description = "Trả về trạng thái chiến dịch, thời gian hiệu lực và số liệu sold/left cho khu vực Flash Sale ở frontend.")
        @ApiResponse(responseCode = "200", description = "Dữ liệu flash sale được trả về thành công")
        @GetMapping("/flash-sale")
        public ResponseEntity<FlashSaleResponse> getFlashSale(
                        @RequestParam(required = false) Integer days,
                        @RequestParam(required = false) Integer limit) {
                Integer resolvedDays = days != null ? days : this.days;
                Integer resolvedLimit = limit != null ? limit : this.limit;
                return ResponseEntity.ok(productService.getFlashSale(resolvedDays, resolvedLimit));
        }
}

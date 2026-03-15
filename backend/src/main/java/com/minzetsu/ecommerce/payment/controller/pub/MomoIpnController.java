package com.minzetsu.ecommerce.payment.controller.pub;

import com.minzetsu.ecommerce.payment.momo.MomoPaymentService;
import com.minzetsu.ecommerce.payment.momo.dto.request.MomoIpnRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/payments/momo")
@RequiredArgsConstructor
@Tag(name = "Public - MoMo IPN", description = "MoMo IPN callback")
public class MomoIpnController {
    private final MomoPaymentService momoPaymentService;

    @Operation(summary = "Handle MoMo IPN")
    @PostMapping("/ipn")
    public ResponseEntity<Void> handleIpn(@RequestBody MomoIpnRequest request) {
        momoPaymentService.handleIpn(request);
        return ResponseEntity.ok().build();
    }
}



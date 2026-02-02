package com.minzetsu.ecommerce.auth.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "email_verification_codes")
@Data
public class EmailVerificationCode extends BaseEntity {
    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "register_payload_json", nullable = false, columnDefinition = "TEXT")
    private String registerPayloadJson;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean verified;
}

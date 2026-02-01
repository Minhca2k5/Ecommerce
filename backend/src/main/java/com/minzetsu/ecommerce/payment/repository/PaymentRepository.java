package com.minzetsu.ecommerce.payment.repository;

import com.minzetsu.ecommerce.payment.entity.Payment;
import com.minzetsu.ecommerce.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByOrderIdOrderByUpdatedAtDesc(Long orderId);

    Payment findByProviderTxnId(String providerTxnId);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.id = :id")
    void updateByStatusAndId(PaymentStatus status, Long id);

    boolean existsById(Long id);
}

package com.minzetsu.ecommerce.notification.repository;

import com.minzetsu.ecommerce.notification.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    List<Notification> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = :isRead WHERE n.id = :id")
    void updateIsReadById(Long id, Boolean isRead);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isHidden = :isHidden WHERE n.id = :id")
    void updateIsHiddenById(Long id, Boolean isHidden);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = :isRead WHERE n.user.id = :userId")
    void updateIsReadByUserId(Long userId, Boolean isRead);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isHidden = :isHidden WHERE n.user.id = :userId")
    void updateIsHiddenByUserId(Long userId, Boolean isHidden);
}

package com.minzetsu.ecommerce.user.repository;

import com.minzetsu.ecommerce.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {
    Optional<Address> findByIsDefaultTrueAndUserId(Long userId);
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = :isDefault WHERE a.id = :id")
    void updateAddressByDefault(Boolean isDefault, Long id);
    boolean existsByUserId(Long userId);
    boolean existsById(Long id);
    boolean existsByIsDefaultTrueAndUserId(Long userId);
    List<Address> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}

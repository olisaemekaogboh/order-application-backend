package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, String> {

    List<DeliveryAddress> findByUserId(String userId);

    List<DeliveryAddress> findByUserIdAndIsDefaultTrue(String userId);

    Optional<DeliveryAddress> findByUserIdAndId(String userId, String addressId);

    boolean existsByUserIdAndIsDefaultTrue(String userId);

    @Query("SELECT a FROM DeliveryAddress a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<DeliveryAddress> findDefaultAddressByUser(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE DeliveryAddress a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultAddresses(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE DeliveryAddress a SET a.isDefault = true WHERE a.id = :addressId AND a.user.id = :userId")
    void setDefaultAddress(@Param("userId") String userId, @Param("addressId") String addressId);

    long countByUserId(String userId);

    @Query("SELECT a FROM DeliveryAddress a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<DeliveryAddress> findByUserIdOrdered(@Param("userId") String userId);
}
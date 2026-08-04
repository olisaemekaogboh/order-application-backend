package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {

    Optional<Driver> findByEmail(String email);

    Optional<Driver> findByPhoneNumber(String phoneNumber);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    Optional<Driver> findFirstByAvailableTrue();

    Optional<Driver> findByVehiclePlateNumber(String vehiclePlateNumber);

    boolean existsByEmail(String email);
    Optional<Driver> findByIdAndEmail(String driverId, String email);

    Optional<Driver> findByIdAndVerifiedTrue(String driverId);

    Page<Driver> findByVerifiedTrue(Pageable pageable);

    List<Driver> findByAvailableTrueAndVerifiedTrueAndVehicleType(VehicleType vehicleType);

    boolean existsByIdAndVerifiedTrue(String driverId);
    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByVehiclePlateNumber(String vehiclePlateNumber);

    List<Driver> findByAvailableTrue();
    Page<Driver> findByAvailableTrue(Pageable pageable);
    List<Driver> findByAvailableTrueAndVerifiedTrue();

    // NEW: Find unavailable drivers
    Page<Driver> findByAvailableFalse(Pageable pageable);

    // NEW: Search drivers by name, email, phone, or license number
    Page<Driver> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrLicenseNumberContainingIgnoreCase(
            String name, String email, String phone, String license, Pageable pageable);

    List<Driver> findByVehicleType(VehicleType vehicleType);

    List<Driver> findByAvailableTrueAndVehicleType(VehicleType vehicleType);

    @Query("SELECT d FROM Driver d WHERE d.available = true AND d.verified = true AND d.rating >= :minRating")
    List<Driver> findAvailableDriversWithMinRating(@Param("minRating") Double minRating);

    @Query("SELECT d FROM Driver d WHERE d.rating >= :minRating")
    List<Driver> findDriversWithMinRating(@Param("minRating") Double minRating);

    @Query("SELECT d FROM Driver d WHERE d.lastActive >= :date")
    List<Driver> findActiveDriversSince(@Param("date") LocalDateTime date);

    @Query("SELECT d FROM Driver d WHERE d.totalDeliveries >= :minDeliveries")
    List<Driver> findExperiencedDrivers(@Param("minDeliveries") Integer minDeliveries);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.available = true")
    long countAvailableDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.verified = true")
    long countVerifiedDrivers();

    @Query("SELECT AVG(d.rating) FROM Driver d WHERE d.verified = true")
    Double calculateAverageRating();

    @Modifying
    @Query("UPDATE Driver d SET d.available = :available WHERE d.id = :driverId")
    void updateAvailability(@Param("driverId") String driverId, @Param("available") boolean available);

    @Modifying
    @Query("UPDATE Driver d SET d.rating = :rating WHERE d.id = :driverId")
    void updateRating(@Param("driverId") String driverId, @Param("rating") Double rating);

    @Modifying
    @Query("UPDATE Driver d SET d.currentLocation = :location, d.currentLatitude = :latitude, d.currentLongitude = :longitude WHERE d.id = :driverId")
    void updateLocation(
            @Param("driverId") String driverId,
            @Param("location") String location,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude
    );

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.available = false AND d.currentLocation IS NOT NULL")
    Long countBusyDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.available = false")
    Long countOfflineDrivers();

    @Query("""
SELECT COUNT(o)
FROM Order o
WHERE o.driver.id = :driverId
AND o.status = com.inkfront.logisticsApplication.domain.enums.OrderStatus.DELIVERED
""")
    Long countCompletedDeliveries(@Param("driverId") String driverId);


}
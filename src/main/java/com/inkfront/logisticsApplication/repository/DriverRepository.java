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

    // ============================================
    // FIND METHODS
    // ============================================

    Optional<Driver> findByUserId(String userId);

    /**
     * Find driver by phone number (using the associated User entity)
     */
    @Query("SELECT d FROM Driver d JOIN d.user u WHERE u.phoneNumber = :phoneNumber")
    Optional<Driver> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    Optional<Driver> findFirstByAvailableTrue();

    Optional<Driver> findByVehiclePlateNumber(String vehiclePlateNumber);

    /**
     * Find driver by email (using the associated User entity)
     */
    @Query("SELECT d FROM Driver d JOIN d.user u WHERE u.email = :email")
    Optional<Driver> findByEmail(@Param("email") String email);

    /**
     * Find driver by ID and email (using the associated User entity)
     */
    @Query("SELECT d FROM Driver d JOIN d.user u WHERE d.id = :id AND u.email = :email")
    Optional<Driver> findByIdAndEmail(@Param("id") String id, @Param("email") String email);

    /**
     * Find driver by ID and verified status
     */
    @Query("SELECT d FROM Driver d WHERE d.id = :driverId AND d.verified = true")
    Optional<Driver> findByIdAndVerifiedTrue(@Param("driverId") String driverId);

    // ============================================
    // EXISTS METHODS
    // ============================================

    /**
     * Check if a driver exists with the given email
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Driver d JOIN d.user u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Check if a driver exists with the given phone number
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Driver d JOIN d.user u WHERE u.phoneNumber = :phoneNumber")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Driver d WHERE d.licenseNumber = :licenseNumber")
    boolean existsByLicenseNumber(@Param("licenseNumber") String licenseNumber);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Driver d WHERE d.vehiclePlateNumber = :vehiclePlateNumber")
    boolean existsByVehiclePlateNumber(@Param("vehiclePlateNumber") String vehiclePlateNumber);

    /**
     * Check if a driver exists with the given ID and is verified
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Driver d WHERE d.id = :driverId AND d.verified = true")
    boolean existsByIdAndVerifiedTrue(@Param("driverId") String driverId);

    // ============================================
    // PAGE AND LIST METHODS
    // ============================================

    @Query("SELECT d FROM Driver d WHERE d.verified = true")
    Page<Driver> findByVerifiedTrue(Pageable pageable);

    @Query("SELECT d FROM Driver d WHERE d.available = true AND d.verified = true AND d.vehicleType = :vehicleType")
    List<Driver> findByAvailableTrueAndVerifiedTrueAndVehicleType(@Param("vehicleType") VehicleType vehicleType);

    @Query("SELECT d FROM Driver d WHERE d.available = true")
    List<Driver> findByAvailableTrue();

    @Query("SELECT d FROM Driver d WHERE d.available = true")
    Page<Driver> findByAvailableTrue(Pageable pageable);

    @Query("SELECT d FROM Driver d WHERE d.available = true AND d.verified = true")
    List<Driver> findByAvailableTrueAndVerifiedTrue();

    @Query("SELECT d FROM Driver d WHERE d.available = false")
    Page<Driver> findByAvailableFalse(Pageable pageable);

    /**
     * Search drivers by name (from User), email (from User), phone (from User), or license number
     * All fields are from the User entity except licenseNumber
     */
    @Query("SELECT d FROM Driver d JOIN d.user u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Driver> searchDrivers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Legacy search method - kept for backward compatibility
     * Fixed to use user fields for name, email, and phone
     */
    @Deprecated
    @Query("SELECT d FROM Driver d JOIN d.user u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) OR " +
            "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :phone, '%')) OR " +
            "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :license, '%'))")
    Page<Driver> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrLicenseNumberContainingIgnoreCase(
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("license") String license,
            Pageable pageable);

    /**
     * Search drivers by name only (using User entity)
     */
    @Query("SELECT d FROM Driver d JOIN d.user u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Driver> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    // ============================================
    // VEHICLE TYPE METHODS
    // ============================================

    @Query("SELECT d FROM Driver d WHERE d.vehicleType = :vehicleType")
    List<Driver> findByVehicleType(@Param("vehicleType") VehicleType vehicleType);

    @Query("SELECT d FROM Driver d WHERE d.available = true AND d.vehicleType = :vehicleType")
    List<Driver> findByAvailableTrueAndVehicleType(@Param("vehicleType") VehicleType vehicleType);

    // ============================================
    // CUSTOM QUERY METHODS
    // ============================================

    @Query("SELECT d FROM Driver d WHERE d.available = true AND d.verified = true AND d.rating >= :minRating")
    List<Driver> findAvailableDriversWithMinRating(@Param("minRating") Double minRating);

    @Query("SELECT d FROM Driver d WHERE d.rating >= :minRating")
    List<Driver> findDriversWithMinRating(@Param("minRating") Double minRating);

    @Query("SELECT d FROM Driver d WHERE d.lastActive >= :date")
    List<Driver> findActiveDriversSince(@Param("date") LocalDateTime date);

    @Query("SELECT d FROM Driver d WHERE d.totalDeliveries >= :minDeliveries")
    List<Driver> findExperiencedDrivers(@Param("minDeliveries") Integer minDeliveries);

    // ============================================
    // COUNT METHODS
    // ============================================

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.available = true")
    long countAvailableDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.verified = true")
    long countVerifiedDrivers();

    @Query("SELECT AVG(d.rating) FROM Driver d WHERE d.verified = true")
    Double calculateAverageRating();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.available = false AND d.currentLocation IS NOT NULL")
    Long countBusyDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.available = false")
    Long countOfflineDrivers();

    // ============================================
    // UPDATE METHODS
    // ============================================

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

    // ============================================
    // ORDER/DELIVERY METHODS
    // ============================================

    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.driver.id = :driverId
            AND o.status = com.inkfront.logisticsApplication.domain.enums.OrderStatus.DELIVERED
            """)
    Long countCompletedDeliveries(@Param("driverId") String driverId);
}
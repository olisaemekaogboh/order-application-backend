package com.inkfront.logisticsApplication.repository.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    Optional<Vehicle> findByVin(String vin);

    boolean existsByVehicleNumber(String vehicleNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByVin(String vin);

    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    Page<Vehicle> findByVehicleType(VehicleType vehicleType, Pageable pageable);

    List<Vehicle> findByStatusAndDeletedFalse(VehicleStatus status);

    @Query("SELECT v FROM Vehicle v WHERE v.status = :status AND v.deleted = false")
    Page<Vehicle> findAvailableVehicles(@Param("status") VehicleStatus status, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenanceDate <= :date AND v.status != 'RETIRED'")
    List<Vehicle> findVehiclesDueForMaintenance(@Param("date") LocalDate date);

    @Query("SELECT v FROM Vehicle v WHERE v.nextInspectionDate <= :date AND v.status != 'RETIRED'")
    List<Vehicle> findVehiclesDueForInspection(@Param("date") LocalDate date);

    @Query("SELECT v FROM Vehicle v WHERE v.insuranceExpiry <= :date AND v.status != 'RETIRED'")
    List<Vehicle> findVehiclesWithExpiringInsurance(@Param("date") LocalDate date);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.deleted = false")
    long countActiveVehicles();

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status AND v.deleted = false")
    long countByStatus(@Param("status") VehicleStatus status);
}
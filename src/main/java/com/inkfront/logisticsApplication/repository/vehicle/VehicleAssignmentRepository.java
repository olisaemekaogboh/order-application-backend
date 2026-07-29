package com.inkfront.logisticsApplication.repository.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignment, String> {

    Optional<VehicleAssignment> findByVehicleIdAndActiveTrue(String vehicleId);

    Optional<VehicleAssignment> findByDriverIdAndActiveTrue(String driverId);

    List<VehicleAssignment> findByVehicleIdOrderByAssignedAtDesc(String vehicleId);

    List<VehicleAssignment> findByDriverIdOrderByAssignedAtDesc(String driverId);

    Page<VehicleAssignment> findByVehicleId(String vehicleId, Pageable pageable);

    Page<VehicleAssignment> findByDriverId(String driverId, Pageable pageable);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.active = true")
    List<VehicleAssignment> findAllActive();

    boolean existsByVehicleIdAndActiveTrue(String vehicleId);
}
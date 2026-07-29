package com.inkfront.logisticsApplication.repository.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleMaintenance;
import com.inkfront.logisticsApplication.domain.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleMaintenanceRepository extends JpaRepository<VehicleMaintenance, String> {

    List<VehicleMaintenance> findByVehicleIdOrderByScheduledDateDesc(String vehicleId);

    Page<VehicleMaintenance> findByVehicleId(String vehicleId, Pageable pageable);

    List<VehicleMaintenance> findByStatus(MaintenanceStatus status);

    @Query("SELECT SUM(vm.cost) FROM VehicleMaintenance vm WHERE vm.vehicle.id = :vehicleId AND vm.status = 'COMPLETED'")
    Double sumMaintenanceCostByVehicle(@Param("vehicleId") String vehicleId);

    @Query("SELECT vm FROM VehicleMaintenance vm WHERE vm.scheduledDate <= :date AND vm.status = 'SCHEDULED'")
    List<VehicleMaintenance> findOverdueMaintenance(@Param("date") LocalDate date);
}
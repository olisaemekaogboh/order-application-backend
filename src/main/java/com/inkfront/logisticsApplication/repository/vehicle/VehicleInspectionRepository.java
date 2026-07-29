package com.inkfront.logisticsApplication.repository.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleInspectionRepository extends JpaRepository<VehicleInspection, String> {

    List<VehicleInspection> findByVehicleIdOrderByInspectionDateDesc(String vehicleId);

    Page<VehicleInspection> findByVehicleId(String vehicleId, Pageable pageable);

    // ✅ FIXED: Use "compliant" instead of "isCompliant"
    @Query("SELECT vi FROM VehicleInspection vi WHERE vi.vehicle.id = :vehicleId AND vi.compliant = false")
    List<VehicleInspection> findNonCompliantInspections(@Param("vehicleId") String vehicleId);

    @Query("SELECT COUNT(vi) FROM VehicleInspection vi WHERE vi.vehicle.id = :vehicleId AND vi.result = 'PASS'")
    long countPassedInspections(@Param("vehicleId") String vehicleId);
}
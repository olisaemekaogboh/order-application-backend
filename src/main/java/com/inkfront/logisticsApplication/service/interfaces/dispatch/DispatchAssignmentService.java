package com.inkfront.logisticsApplication.service.interfaces.dispatch;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchAssignmentResult;

import java.util.List;

public interface DispatchAssignmentService {

    List<Driver> findAvailableDriversForDispatch(String orderId);

    List<Vehicle> findAvailableVehiclesForDispatch(String orderId);

    Driver assignBestDriver(String orderId);

    Vehicle assignBestVehicle(String orderId);

    DispatchAssignmentResult assignBestDriverAndVehicle(Dispatch dispatch);

    boolean validateDriverAssignment(Driver driver, String orderId);

    boolean validateVehicleAssignment(Vehicle vehicle, String orderId);

    boolean isDriverAvailableForDispatch(String driverId, String orderId);

    boolean isVehicleAvailableForDispatch(String vehicleId, String orderId);
}
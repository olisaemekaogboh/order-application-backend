package com.inkfront.logisticsApplication.service.interfaces.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;

/**
 * Service responsible for sending vehicle-related notifications.
 */
public interface VehicleNotificationService {

    /**
     * Send reminders for vehicles due for maintenance.
     */
    void sendMaintenanceReminders();

    /**
     * Send reminders for vehicles due for inspection.
     */
    void sendInspectionReminders();

    /**
     * Send reminders for vehicles with expiring insurance.
     */
    void sendInsuranceReminders();

    /**
     * Notify when a vehicle is assigned to a driver.
     *
     * @param vehicle the assigned vehicle
     * @param driverName the name of the driver
     */
    void notifyVehicleAssigned(Vehicle vehicle, String driverName);
}
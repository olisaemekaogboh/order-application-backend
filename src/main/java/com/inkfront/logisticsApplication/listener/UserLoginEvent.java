package com.inkfront.logisticsApplication.listener;


import com.inkfront.logisticsApplication.domain.entity.User;
import lombok.Getter;

@Getter
public class UserLoginEvent {
    private final User user;
    private final String ipAddress;
    private final boolean newDevice;

    public UserLoginEvent(User user, String ipAddress, boolean newDevice) {
        this.user = user;
        this.ipAddress = ipAddress;
        this.newDevice = newDevice;
    }
}

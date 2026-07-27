package com.inkfront.logisticsApplication.listener;


import com.inkfront.logisticsApplication.domain.entity.User;
import lombok.Getter;

@Getter
public class UserAccountLockedEvent {
    private final User user;
    private final String reason;

    public UserAccountLockedEvent(User user, String reason) {
        this.user = user;
        this.reason = reason;
    }
}

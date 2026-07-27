package com.inkfront.logisticsApplication.listener;


import com.inkfront.logisticsApplication.domain.entity.User;
import lombok.Getter;

@Getter
public class UserAccountUnlockedEvent {
    private final User user;

    public UserAccountUnlockedEvent(User user) {
        this.user = user;
    }
}
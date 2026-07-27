
// UserEvent classes
package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.domain.entity.User;
import lombok.Getter;

@Getter
public class UserPasswordResetEvent {
    private final User user;
    private final String token;

    public UserPasswordResetEvent(User user, String token) {
        this.user = user;
        this.token = token;
    }
}

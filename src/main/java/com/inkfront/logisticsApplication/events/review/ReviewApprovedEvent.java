package com.inkfront.logisticsApplication.events.review;

import com.inkfront.logisticsApplication.domain.entity.Review;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewApprovedEvent extends ApplicationEvent {
    private final Review review;

    public ReviewApprovedEvent(Object source, Review review) {
        super(source);
        this.review = review;
    }
}
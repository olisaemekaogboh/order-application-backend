package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.Review;
import com.inkfront.logisticsApplication.dto.request.review.ReviewRequestDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewResponseDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.fullName")
    ReviewResponseDTO toResponseDTO(Review review);

    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "driverName", source = "driver.name")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "commentSnippet", expression = "java(review.getComment() != null && review.getComment().length() > 50 ? review.getComment().substring(0, 50) + \"...\" : review.getComment())")
    ReviewSummaryDTO toSummaryDTO(Review review);

    List<ReviewSummaryDTO> toSummaryDTOList(List<Review> reviews);

    Review toEntity(ReviewRequestDTO dto);

    void updateReviewFromDTO(ReviewRequestDTO dto, @MappingTarget Review review);
}
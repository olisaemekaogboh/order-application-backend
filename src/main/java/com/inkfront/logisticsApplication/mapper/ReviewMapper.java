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

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.fullName")
    ReviewResponseDTO toResponseDTO(Review review);

    @Mapping(target = "orderNumber", source = "order.orderNumber")

    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "commentSnippet", expression = "java(review.getComment() != null && review.getComment().length() > 50 ? review.getComment().substring(0, 50) + \"...\" : review.getComment())")
    // ADD THESE MAPPINGS FOR THE MISSING FIELDS
    @Mapping(target = "moderationStatus", expression = "java(review.getModerationStatus() != null ? review.getModerationStatus().name() : null)")
    @Mapping(target = "reviewStatus", expression = "java(review.getReviewStatus() != null ? review.getReviewStatus().name() : null)")
    @Mapping(target = "reported", source = "reported")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "createdAt", source = "createdAt")
    ReviewSummaryDTO toSummaryDTO(Review review);

    List<ReviewSummaryDTO> toSummaryDTOList(List<Review> reviews);

    Review toEntity(ReviewRequestDTO dto);

    void updateReviewFromDTO(ReviewRequestDTO dto, @MappingTarget Review review);
}
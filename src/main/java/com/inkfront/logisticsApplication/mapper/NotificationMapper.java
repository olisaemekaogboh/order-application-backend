package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.Notification;
import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class NotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    public abstract NotificationDTO toDTO(Notification notification);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "read", constant = "false")
    @Mapping(target = "delivered", constant = "false")
    public abstract Notification toEntity(NotificationDTO notificationDTO);

    public abstract List<NotificationDTO> toDTOList(List<Notification> notifications);
}
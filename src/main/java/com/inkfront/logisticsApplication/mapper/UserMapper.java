package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.DeliveryAddress;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.dto.request.user.UserUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.user.AddressDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {

    @Autowired
    protected AddressMapper addressMapper;

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "addresses", expression = "java(mapAddressesToDTO(user.getAddresses()))")
    @Mapping(target = "totalOrders", expression = "java(user.getOrders() != null ? user.getOrders().size() : 0)")
    @Mapping(target = "totalSpent", expression = "java(calculateTotalSpent(user))")
    public abstract UserDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)

    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "emailVerificationTokens", ignore = true)
    @Mapping(target = "passwordResetTokens", ignore = true)

    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "failedAttempts", ignore = true)
    @Mapping(target = "lockTime", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "googleAuth", ignore = true)
    @Mapping(target = "googleId", ignore = true)

    public abstract User toEntity(UserUpdateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)

    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "emailVerificationTokens", ignore = true)
    @Mapping(target = "passwordResetTokens", ignore = true)

    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "failedAttempts", ignore = true)
    @Mapping(target = "lockTime", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "googleAuth", ignore = true)
    @Mapping(target = "googleId", ignore = true)

    public abstract void updateUserFromDTO(
            UserUpdateRequestDTO dto,
            @MappingTarget User user
    );

    public abstract List<UserDTO> toDTOList(List<User> users);

    protected List<AddressDTO> mapAddressesToDTO(List<DeliveryAddress> addresses) {
        if (addresses == null) {
            return null;
        }

        return addresses.stream()
                .map(addressMapper::toDTO)
                .collect(Collectors.toList());
    }

    protected Double calculateTotalSpent(User user) {
        if (user.getOrders() == null || user.getOrders().isEmpty()) {
            return 0.0;
        }

        return user.getOrders().stream()
                .filter(order -> order.isDelivered() && order.isPaid())
                .mapToDouble(order -> order.getTotalPrice())
                .sum();
    }
}
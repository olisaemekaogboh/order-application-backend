// mapper/AddressMapper.java
package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.DeliveryAddress;
import com.inkfront.logisticsApplication.dto.request.user.AddressRequestDTO;
import com.inkfront.logisticsApplication.dto.response.user.AddressDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AddressMapper {

    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    public abstract AddressDTO toDTO(DeliveryAddress address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    public abstract DeliveryAddress toEntity(AddressRequestDTO addressRequestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    public abstract void updateAddressFromDTO(AddressRequestDTO dto, @MappingTarget DeliveryAddress address);

    public abstract List<AddressDTO> toDTOList(List<DeliveryAddress> addresses);
}
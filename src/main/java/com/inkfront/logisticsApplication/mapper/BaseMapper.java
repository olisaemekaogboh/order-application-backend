package com.inkfront.logisticsApplication.mapper;

import org.mapstruct.*;

import java.util.List;

public interface BaseMapper<E, D> {

    D toDTO(E entity);

    E toEntity(D dto);

    List<D> toDTOList(List<E> entities);

    List<E> toEntityList(List<D> dtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(D dto, @MappingTarget E entity);
}
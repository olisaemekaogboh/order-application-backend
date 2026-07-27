package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaginationMapper {

    public <T> PaginatedResponseDTO<T> toPaginatedResponse(Page<T> page) {
        return new PaginatedResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public <E, D> PaginatedResponseDTO<D> toPaginatedResponse(Page<E> page, List<D> content) {
        return new PaginatedResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
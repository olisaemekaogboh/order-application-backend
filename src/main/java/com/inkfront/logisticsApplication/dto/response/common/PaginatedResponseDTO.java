// dto/response/common/PaginatedResponseDTO.java
package com.inkfront.logisticsApplication.dto.response.common;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedResponseDTO<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;
    private int numberOfElements;

    public PaginatedResponseDTO(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.first = pageNumber == 0;
        this.last = (pageNumber + 1) == totalPages;
        this.empty = content.isEmpty();
        this.numberOfElements = content.size();
    }
}
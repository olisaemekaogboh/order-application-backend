package com.inkfront.logisticsApplication.dto.response.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportResponseDTO {

    private String fileName;
    private String fileType;
    private Long recordCount;
    private String downloadUrl;  // or the actual file content as base64
}
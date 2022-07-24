package com.newrelic.aws.proxy.service.create.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidateRequestDto {

    private String customItemName;
    private String customItemDescription;
    private String customItemRequestTimestamp;
}

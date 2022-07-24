package com.newrelic.aws.proxy.service.create.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResult {

    private String validationId;
    private List<String> customItemInvalidReasons;

    private String customItemName;
    private String customItemDescription;
    private String customItemRequestTimestamp;
    private String customItemValidationTimestamp;
}

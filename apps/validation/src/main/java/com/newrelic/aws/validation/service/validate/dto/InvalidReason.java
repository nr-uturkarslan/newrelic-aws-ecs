package com.newrelic.aws.validation.service.validate.dto;

public enum InvalidReason {
    NAME_NOT_PROVIDED("nameNotProvided"),
    DESCRIPTION_NOT_PROVIDED("descriptionNotProvided"),
    REQUEST_TIMESTAMP_NOT_PROVIDED("requestTimestampNotProvided");

    private String value;
    InvalidReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

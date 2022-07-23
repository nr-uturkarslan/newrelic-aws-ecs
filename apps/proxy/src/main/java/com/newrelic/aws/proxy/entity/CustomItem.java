package com.newrelic.aws.proxy.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomItem {

    private String id;
    private String name;
    private String description;
    private String requestTimestamp;
    private String validationTimestamp;
    private boolean isValid;
}

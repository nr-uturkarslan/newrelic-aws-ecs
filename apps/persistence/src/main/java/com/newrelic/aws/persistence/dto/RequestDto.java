package com.newrelic.aws.persistence.dto;

import com.newrelic.aws.persistence.entity.CustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private CustomItem customItem;
}

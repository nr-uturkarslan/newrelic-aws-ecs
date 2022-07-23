package com.newrelic.aws.proxy.service.create.dto;

import com.newrelic.aws.proxy.entity.CustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDto {
    private CustomItem customItem;
}

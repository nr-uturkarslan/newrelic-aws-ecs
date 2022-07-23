package com.newrelic.aws.persistence.service.create.dto;

import com.newrelic.aws.persistence.entity.CustomItem;
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

package com.newrelic.aws.persistence.controller;

import com.newrelic.aws.persistence.dto.RequestDto;
import com.newrelic.aws.persistence.dto.ResponseDto;
import com.newrelic.aws.persistence.entity.CustomItem;
import com.newrelic.aws.persistence.service.create.CreateCustomItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("persistence")
public class PersistenceController {

    private final Logger logger = LoggerFactory.getLogger(PersistenceController.class);

    @Autowired
    private CreateCustomItemService createCustomItemService;

    @PostMapping("create")
    public ResponseEntity<ResponseDto<CustomItem>> create(
            @RequestHeader Map<String, String> headers,
            @RequestBody RequestDto requestDto
    ) {
        logger.info("Create method is triggered...");

        for (var header : headers.entrySet()) {
            logger.info("Key:" + header.getKey());
            logger.info("Value:" + header.getValue());
        }

        if (headers.containsKey("x-correlation-id"))
            logger.info("Correlation ID: " + headers.get("x-correlation-id"));

        var responseDto = createCustomItemService.run(requestDto);

        logger.info("Create method is executed.");

        return responseDto;
    }
}

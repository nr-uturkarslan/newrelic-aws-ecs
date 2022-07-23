package com.newrelic.aws.persistence.controller;

import com.newrelic.aws.persistence.dto.ResponseDto;
import com.newrelic.aws.persistence.service.create.CreateCustomItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("health")
public class HealthController {

    private final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private CreateCustomItemService createCustomItemService;

    @GetMapping()
    public ResponseEntity<ResponseDto<String>> health() {

        var responseDto = new ResponseDto<String>();
        responseDto.setMessage("OK");
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}

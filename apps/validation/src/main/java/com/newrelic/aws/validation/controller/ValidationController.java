package com.newrelic.aws.validation.controller;

import com.newrelic.aws.validation.dto.ResponseDto;
import com.newrelic.aws.validation.entity.ValidationResult;
import com.newrelic.aws.validation.service.validate.dto.ValidateRequestDto;
import com.newrelic.aws.validation.service.validate.ValidateCustomItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("validation")
public class ValidationController {

    private final Logger logger = LoggerFactory.getLogger(ValidationController.class);

    @Autowired
    private ValidateCustomItemService validateCustomItemService;

    @GetMapping("health")
    public ResponseEntity<ResponseDto<String>> health() {

        var responseDto = new ResponseDto<String>();
        responseDto.setMessage("OK");
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("validate")
    public ResponseEntity<ResponseDto<ValidationResult>> create(
            @RequestBody ValidateRequestDto validateRequestDto
    ) {
        logger.info("message:Validate method is triggered...");
        var response = validateCustomItemService.run(validateRequestDto);

        logger.info("message:Validate method is executed.");
        return response;
    }
}

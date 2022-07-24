package com.newrelic.aws.validation.service.validate;

import com.newrelic.aws.validation.dto.ResponseDto;
import com.newrelic.aws.validation.entity.ValidationResult;
import com.newrelic.aws.validation.service.validate.dto.InvalidReason;
import com.newrelic.aws.validation.service.validate.dto.ValidateRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class ValidateCustomItemService {

    private final Logger logger = LoggerFactory.getLogger(ValidateCustomItemService.class);

    public ValidateCustomItemService() {}

    public ResponseEntity<ResponseDto<ValidationResult>> run(
            ValidateRequestDto validateRequestDto
    ) {
        logger.info("message:Validating custom item...");

        var validationResult = new ValidationResult();
        validationResult.setValidationId(UUID.randomUUID().toString());
        validationResult.setCustomItemInvalidReasons(new ArrayList<>());

        // Name
        validateCustomItemName(validateRequestDto, validationResult);

        // Description
        validateCustomItemDescription(validateRequestDto, validationResult);

        // Request Timestamp
        validateCustomItemRequestTimestamp(validateRequestDto, validationResult);

        logger.info("message:Custom item is validated.");

        var responseDto = new ResponseDto<ValidationResult>();
        responseDto.setData(validationResult);

        if (validationResult.getCustomItemInvalidReasons().isEmpty()) {
            logger.info("message:Custom is valid.");
            responseDto.setMessage("Custom is valid.");
            return new ResponseEntity<>(responseDto, HttpStatus.ACCEPTED);
        }
        else {
            logger.warn("message:Custom is invalid.");
            responseDto.setMessage("Custom is invalid.");
            return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateCustomItemName(
            ValidateRequestDto validateRequestDto,
            ValidationResult validationResult
    ) {
        if (validateRequestDto.getCustomItemName() == null ||
                validateRequestDto.getCustomItemName().isEmpty()) {
            logger.warn("message:Custom item name is not provided..." +
                    "validationId:" + validationResult.getValidationId());
            validationResult.getCustomItemInvalidReasons()
                    .add(InvalidReason.NAME_NOT_PROVIDED.getValue());
        }
    }

    private void validateCustomItemDescription(
            ValidateRequestDto validateRequestDto,
            ValidationResult validationResult
    ){
        if (validateRequestDto.getCustomItemDescription() == null ||
                validateRequestDto.getCustomItemDescription().isEmpty()) {
            logger.warn("message:Custom item description is not provided..." +
                    "validationId:" + validationResult.getValidationId());
            validationResult.getCustomItemInvalidReasons()
                    .add(InvalidReason.DESCRIPTION_NOT_PROVIDED.getValue());
        }
    }

    private void validateCustomItemRequestTimestamp(
            ValidateRequestDto validateRequestDto,
            ValidationResult validationResult
    ){
        if (validateRequestDto.getCustomItemRequestTimestamp() == null ||
                validateRequestDto.getCustomItemRequestTimestamp().isEmpty()) {
            logger.warn("message:Custom item request timestamp is not provided..." +
                    "validationId:" + validationResult.getValidationId());
            validationResult.getCustomItemInvalidReasons()
                    .add(InvalidReason.REQUEST_TIMESTAMP_NOT_PROVIDED.getValue());
        }
    }
}
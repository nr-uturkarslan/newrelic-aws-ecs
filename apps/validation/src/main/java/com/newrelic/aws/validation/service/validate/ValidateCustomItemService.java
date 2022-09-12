package com.newrelic.aws.validation.service.validate;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.gson.Gson;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.aws.validation.dto.ResponseDto;
import com.newrelic.aws.validation.entity.ValidationResult;
import com.newrelic.aws.validation.service.validate.dto.InvalidReason;
import com.newrelic.aws.validation.service.validate.dto.ValidateRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class ValidateCustomItemService {

    private final Logger logger = LoggerFactory.getLogger(ValidateCustomItemService.class);

    @Autowired
    private AmazonS3 amazonS3;

    private final Gson gson = new Gson();

    public ValidateCustomItemService() {}

    public ResponseEntity<ResponseDto<ValidationResult>> run(
            ValidateRequestDto validateRequestDto
    ) {
        logger.info("Validating custom item...");

        var validationResult = new ValidationResult();
        validationResult.setValidationId(UUID.randomUUID().toString());
        validationResult.setCustomItemInvalidReasons(new ArrayList<>());

        var trace = NewRelic.getAgent().getTracedMethod();
        trace.addCustomAttribute("customItemValidationId", validationResult.getValidationId());

        // Name
        validateCustomItemName(validateRequestDto, validationResult);

        // Description
        validateCustomItemDescription(validateRequestDto, validationResult);

        // Request Timestamp
        validateCustomItemRequestTimestamp(validateRequestDto, validationResult);

        validationResult.setCustomItemValidationTimestamp(
                new Timestamp(System.currentTimeMillis()).toString()
        );
        logger.info("Custom item is validated.");

        var responseDto = new ResponseDto<ValidationResult>();
        responseDto.setData(validationResult);

        if (validationResult.getCustomItemInvalidReasons().isEmpty()) {
            logger.info("Custom item is valid.");
            responseDto.setMessage("Custom item is valid.");
            return new ResponseEntity<>(responseDto, HttpStatus.ACCEPTED);
        }
        else {
            logger.warn("Custom item is invalid.");
            saveInvalidCustomItemToS3(validationResult);

            responseDto.setMessage("Custom item is invalid.");
            return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateCustomItemName(
            ValidateRequestDto validateRequestDto,
            ValidationResult validationResult
    ) {
        if (validateRequestDto.getCustomItemName() == null ||
                validateRequestDto.getCustomItemName().isEmpty()) {
            logger.warn("Custom item name is not provided..." +
                    "validationId:" + validationResult.getValidationId());
            validationResult.getCustomItemInvalidReasons()
                    .add(InvalidReason.NAME_NOT_PROVIDED.getValue());
            var trace = NewRelic.getAgent().getTracedMethod();
            trace.addCustomAttribute("customItemInvalidReason",
                    InvalidReason.NAME_NOT_PROVIDED.getValue());
        }
    }

    private void validateCustomItemDescription(
            ValidateRequestDto validateRequestDto,
            ValidationResult validationResult
    ){
        if (validateRequestDto.getCustomItemDescription() == null ||
                validateRequestDto.getCustomItemDescription().isEmpty()) {
            logger.warn("Custom item description is not provided..." +
                    "validationId:" + validationResult.getValidationId());
            validationResult.getCustomItemInvalidReasons()
                    .add(InvalidReason.DESCRIPTION_NOT_PROVIDED.getValue());
            var trace = NewRelic.getAgent().getTracedMethod();
            trace.addCustomAttribute("customItemInvalidReason",
                    InvalidReason.DESCRIPTION_NOT_PROVIDED.getValue());
        }
    }

    private void validateCustomItemRequestTimestamp(
            ValidateRequestDto validateRequestDto,
            ValidationResult validationResult
    ){
        if (validateRequestDto.getCustomItemRequestTimestamp() == null ||
                validateRequestDto.getCustomItemRequestTimestamp().isEmpty()) {
            logger.warn("Custom item request timestamp is not provided..." +
                    "validationId:" + validationResult.getValidationId());
            validationResult.getCustomItemInvalidReasons()
                    .add(InvalidReason.REQUEST_TIMESTAMP_NOT_PROVIDED.getValue());
            var trace = NewRelic.getAgent().getTracedMethod();
            trace.addCustomAttribute("customItemInvalidReason",
                    InvalidReason.REQUEST_TIMESTAMP_NOT_PROVIDED.getValue());
        }
    }

    private void saveInvalidCustomItemToS3(
            ValidationResult validationResult
    ) {
        try {

            logger.info("Putting invalid custom item into S3 bucket...");

            var validationResultAsString = gson.toJson(validationResult);
            logger.info("Validation result as string ->" + validationResultAsString);

            var inputStream = new ByteArrayInputStream(
                    validationResultAsString.getBytes());
            logger.info("Converted to input stream.");

            var putObjectRequest = new PutObjectRequest(
                    "invalid-custom-items",
                    validationResult.getValidationId() + ".json",
                    inputStream,
                    new ObjectMetadata()
            );
            logger.info("PutObjectRequest is created.");

            amazonS3.putObject(putObjectRequest);
            logger.info("Invalid custom item is put into S3 bucket.");
        }
        catch (AmazonServiceException e) {
            logger.info("Error is occurred by putting validation result into S3");
            logger.error(e.getErrorMessage());
        }
    }
}
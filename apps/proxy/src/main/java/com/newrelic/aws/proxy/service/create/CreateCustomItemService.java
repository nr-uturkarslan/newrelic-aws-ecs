package com.newrelic.aws.proxy.service.create;

import com.newrelic.aws.proxy.dto.ResponseDto;
import com.newrelic.aws.proxy.entity.CustomItem;
import com.newrelic.aws.proxy.service.create.dto.CreateRequestDto;
import com.newrelic.aws.proxy.service.create.dto.CreateResponseDto;
import com.newrelic.aws.proxy.service.create.dto.ValidateRequestDto;
import com.newrelic.aws.proxy.service.create.dto.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class CreateCustomItemService {

    private final Logger logger = LoggerFactory.getLogger(CreateCustomItemService.class);

    @Autowired
    private RestTemplate restTemplate;

    public CreateCustomItemService() {}

    public ResponseEntity<ResponseDto<CreateResponseDto>> run(
            Map<String, String> headers,
            CreateRequestDto createRequestDto
    ) {
        var validationResponse = makeRequestToValidationService(
                headers, createRequestDto);
        if (validationResponse.getStatusCode() != HttpStatus.ACCEPTED) {
            logger.error("message:Custom item is not valid.");

            var data = new CreateResponseDto();
            data.setValidationResult(validationResponse.getBody().getData());

            var responseDto = new ResponseDto<CreateResponseDto>();
            responseDto.setMessage(validationResponse.getBody().getMessage());
            responseDto.setData(data);

            return new ResponseEntity<>(responseDto, validationResponse.getStatusCode());
        }
        else {
            logger.info("message:Custom item is valid.");
            createRequestDto.getCustomItem().setValidationTimestamp(
                    validationResponse.getBody().getData().getCustomItemValidationTimestamp()
            );
        }

        var persistenceResponse = makeRequestToPersistenceService(
                headers, createRequestDto);

        var data = new CreateResponseDto();
        data.setCustomItem(persistenceResponse.getBody().getData());

        var responseDto = new ResponseDto<CreateResponseDto>();
        responseDto.setMessage(persistenceResponse.getBody().getMessage());
        responseDto.setData(data);

        return new ResponseEntity<>(responseDto, persistenceResponse.getStatusCode());
    }

    private ResponseEntity<ResponseDto<ValidationResult>> makeRequestToValidationService(
            Map<String, String> headers,
            CreateRequestDto createRequestDto
    ) {
        logger.info("message:Making request to validation service...");

        var validateRequestDto = new ValidateRequestDto();
        validateRequestDto.setCustomItemName(
                createRequestDto.getCustomItem().getName()
        );
        validateRequestDto.setCustomItemDescription(
                createRequestDto.getCustomItem().getDescription()
        );
        validateRequestDto.setCustomItemRequestTimestamp(
                createRequestDto.getCustomItem().getRequestTimestamp()
        );

        var loadBalancerUrl = "http://" + System.getenv("LOAD_BALANCER_URL");
        var persistenceCreateUrl = loadBalancerUrl + "/validation/validate";

        var validationHeaders = new HttpHeaders();
        validationHeaders.setContentType(MediaType.APPLICATION_JSON);
        validationHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        if (headers.containsKey("x-user-name"))
            validationHeaders.set("x-user-name", headers.get("x-user-name"));
        if (headers.containsKey("x-user-department"))
            validationHeaders.set("x-user-department", headers.get("x-user-department"));

        var entity = new HttpEntity<>(validateRequestDto, validationHeaders);
        var response = restTemplate.exchange(persistenceCreateUrl, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseDto<ValidationResult>>() {});

        logger.info("message:Request to validation service is made.");
        return response;
    }

    private ResponseEntity<ResponseDto<CustomItem>> makeRequestToPersistenceService(
            Map<String, String> headers,
            CreateRequestDto createRequestDto
    ) {
        logger.info("message:Making request to persistence service...");

        var loadBalancerUrl = "http://" + System.getenv("LOAD_BALANCER_URL");
        var persistenceCreateUrl = loadBalancerUrl + "/persistence/create";

        var persistenceHeaders = new HttpHeaders();
        persistenceHeaders.setContentType(MediaType.APPLICATION_JSON);
        persistenceHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        if (headers.containsKey("x-user-name"))
            persistenceHeaders.set("x-user-name", headers.get("x-user-name"));
        if (headers.containsKey("x-user-department"))
            persistenceHeaders.set("x-user-department", headers.get("x-user-department"));

        var entity = new HttpEntity<>(createRequestDto, persistenceHeaders);
        var response = restTemplate.exchange(persistenceCreateUrl, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseDto<CustomItem>>() {});

        logger.info("message:Request to persistence service is made.");
        return response;
    }
}

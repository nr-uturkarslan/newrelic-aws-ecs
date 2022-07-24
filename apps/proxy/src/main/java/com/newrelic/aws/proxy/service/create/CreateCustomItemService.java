package com.newrelic.aws.proxy.service.create;

import com.newrelic.aws.proxy.dto.ResponseDto;
import com.newrelic.aws.proxy.entity.CustomItem;
import com.newrelic.aws.proxy.service.create.dto.CreateRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class CreateCustomItemService {

    private final Logger logger = LoggerFactory.getLogger(CreateCustomItemService.class);

    @Autowired
    private RestTemplate restTemplate;

    public CreateCustomItemService() {}

    public ResponseEntity<ResponseDto<CustomItem>> run(
            CreateRequestDto createRequestDto
    ) {
        logger.info("message:Making request to persistence service...");
        var response = makeRequestToPersistenceService(createRequestDto);

        logger.info("message:Request to persistence service is made.");
        return response;
    }

    private ResponseEntity<ResponseDto<CustomItem>> makeRequestToPersistenceService(
            CreateRequestDto createRequestDto
    ) {
        var loadBalancerUrl = "http://" + System.getenv("LOAD_BALANCER_URL");
        var persistenceCreateUrl = loadBalancerUrl + "/persistence/create";
        logger.info("message:Persistence (create) URL is " + persistenceCreateUrl);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        var entity = new HttpEntity<>(createRequestDto, headers);
        return restTemplate.exchange(persistenceCreateUrl, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseDto<CustomItem>>() {});
    }
}

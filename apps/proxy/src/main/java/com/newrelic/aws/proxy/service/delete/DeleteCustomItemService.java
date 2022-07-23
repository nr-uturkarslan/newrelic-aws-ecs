package com.newrelic.aws.proxy.service.delete;

import com.newrelic.aws.proxy.dto.ResponseDto;
import com.newrelic.aws.proxy.entity.CustomItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class DeleteCustomItemService {
    private final Logger logger = LoggerFactory.getLogger(DeleteCustomItemService.class);

    @Autowired
    private RestTemplate restTemplate;

    public DeleteCustomItemService() {}

    public ResponseEntity<ResponseDto<CustomItem>> run(
            String customItemId
    ) {
        logger.info("message:Making request to persistence service...");
        var response = makeRequestToPersistenceService(customItemId);

        logger.info("message:Request to persistence service is made.");
        return response;
    }

    private ResponseEntity<ResponseDto<CustomItem>> makeRequestToPersistenceService(
            String customItemId
    ) {
        var loadBalancerUrl = System.getenv("LOAD_BALANCER_URL");
        var url = loadBalancerUrl + "/persistence?customItemId=" + customItemId;

        return restTemplate.exchange(url, HttpMethod.DELETE, null,
                new ParameterizedTypeReference<ResponseDto<CustomItem>>() {});
    }
}

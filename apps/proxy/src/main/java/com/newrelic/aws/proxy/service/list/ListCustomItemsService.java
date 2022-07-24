package com.newrelic.aws.proxy.service.list;

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
import java.util.List;

@Service
public class ListCustomItemsService {

    private final Logger logger = LoggerFactory.getLogger(ListCustomItemsService.class);

    @Autowired
    private RestTemplate restTemplate;

    public ListCustomItemsService() {}

    public ResponseEntity<ResponseDto<List<CustomItem>>> run(
            Integer limit
    ) {
        logger.info("message:Making request to persistence service...");
        var response = makeRequestToPersistenceService(limit);

        logger.info("message:Request to persistence service is made.");
        return response;
    }

    private ResponseEntity<ResponseDto<List<CustomItem>>> makeRequestToPersistenceService(
            Integer limit
    ) {
        var loadBalancerUrl = "http://" + System.getenv("LOAD_BALANCER_URL");
        var persistenceListUrl = loadBalancerUrl + "/persistence/list?limit=" + limit;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        var entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(persistenceListUrl, HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseDto<List<CustomItem>>>() {});
    }
}

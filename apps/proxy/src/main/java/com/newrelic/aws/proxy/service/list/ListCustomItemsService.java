package com.newrelic.aws.proxy.service.list;

import com.newrelic.aws.proxy.dto.ResponseDto;
import com.newrelic.aws.proxy.entity.CustomItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        var loadBalancerUrl = System.getenv("LOAD_BALANCER_URL");
        var url = loadBalancerUrl + "/persistence/delete?limit=" + limit;

        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<ResponseDto<List<CustomItem>>>() {});
    }
}

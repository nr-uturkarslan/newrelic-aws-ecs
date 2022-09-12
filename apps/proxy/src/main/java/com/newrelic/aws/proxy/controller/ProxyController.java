package com.newrelic.aws.proxy.controller;

import com.newrelic.aws.proxy.dto.ResponseDto;
import com.newrelic.aws.proxy.entity.CustomItem;
import com.newrelic.aws.proxy.service.create.CreateCustomItemService;
import com.newrelic.aws.proxy.service.create.dto.CreateRequestDto;
import com.newrelic.aws.proxy.service.create.dto.CreateResponseDto;
import com.newrelic.aws.proxy.service.delete.DeleteCustomItemService;
import com.newrelic.aws.proxy.service.list.ListCustomItemsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("proxy")
public class ProxyController {

    private final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @Autowired
    private CreateCustomItemService createCustomItemService;

    @Autowired
    private ListCustomItemsService listCustomItemsService;

    @Autowired
    private DeleteCustomItemService deleteCustomItemService;

    @GetMapping("health")
    public ResponseEntity<ResponseDto<String>> health() {

        var responseDto = new ResponseDto<String>();
        responseDto.setMessage("OK");
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("create")
    public ResponseEntity<ResponseDto<CreateResponseDto>> create(
            @RequestHeader Map<String, String> headers,
            @RequestBody CreateRequestDto createRequestDto
    ) {
        logger.info("Create method is triggered...");
        var responseDto = createCustomItemService.run(headers, createRequestDto);

        logger.info("Create method is executed.");
        return responseDto;
    }

    @GetMapping("list")
    public ResponseEntity<ResponseDto<List<CustomItem>>> list(
            @RequestParam(
                    name = "limit",
                    defaultValue = "5",
                    required = false
            ) Integer limit
    ) {
        logger.info("List method is triggered...");

        var responseDto = listCustomItemsService.run(limit);

        logger.info("List method is executed.");

        return responseDto;
    }

    @DeleteMapping("delete")
    public ResponseEntity<ResponseDto<CustomItem>> create(
            @RequestParam String customItemId
    ) {
        logger.info("Delete method is triggered...");

        var responseDto = deleteCustomItemService.run(customItemId);

        logger.info("Delete method is executed.");

        return responseDto;
    }
}

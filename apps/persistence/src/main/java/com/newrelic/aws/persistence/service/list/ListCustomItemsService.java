package com.newrelic.aws.persistence.service.list;

import com.newrelic.aws.persistence.dto.ResponseDto;
import com.newrelic.aws.persistence.entity.CustomItem;
import com.newrelic.aws.persistence.repository.CustomItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListCustomItemsService {

    private final Logger logger = LoggerFactory.getLogger(ListCustomItemsService.class);

    @Autowired
    private CustomItemRepository customItemRepository;

    public ListCustomItemsService() {}

    public ResponseEntity<ResponseDto<List<CustomItem>>> run(
            Integer providedLimit
    ) {
        logger.info("message:Retrieving custom items...");

        var limit = 5;
        if (providedLimit == null)
            logger.info("message:Limit is not provided. Set to 5 as default.");
        else if (providedLimit == 0)
            logger.warn("message:Limit cannot be 0. Set to 5 as default.");
        else
            limit = providedLimit;

        var customItems = customItemRepository.getCustomItems(limit);
        logger.info("message:Custom items are retrieved.");

        var responseDto = new ResponseDto<List<CustomItem>>();
        responseDto.setMessage("Custom items are retrieved.");
        responseDto.setData(customItems);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}

package com.newrelic.aws.persistence.service.create;

import com.newrelic.aws.persistence.service.create.dto.CreateRequestDto;
import com.newrelic.aws.persistence.dto.ResponseDto;
import com.newrelic.aws.persistence.entity.CustomItem;
import com.newrelic.aws.persistence.repository.CustomItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Service
public class CreateCustomItemService {

    private final Logger logger = LoggerFactory.getLogger(CreateCustomItemService.class);

    @Autowired
    private CustomItemRepository customItemRepository;

    public CreateCustomItemService() {}

    public ResponseEntity<ResponseDto<CustomItem>> run(
            CreateRequestDto createRequestDto
    ) {
        logger.info("message:Creating custom item...");
        var customItem = createRequestDto.getCustomItem();
        customItem.setId(UUID.randomUUID().toString());

        customItemRepository.saveCustomItem(customItem);
        logger.info("message:Custom item is created.");

        var responseDto = new ResponseDto<CustomItem>();
        responseDto.setMessage("Custom item is created.");
        responseDto.setData(customItem);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}

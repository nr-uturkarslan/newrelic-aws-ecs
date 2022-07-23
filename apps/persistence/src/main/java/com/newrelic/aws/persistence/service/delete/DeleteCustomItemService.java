package com.newrelic.aws.persistence.service.delete;

import com.newrelic.aws.persistence.dto.ResponseDto;
import com.newrelic.aws.persistence.entity.CustomItem;
import com.newrelic.aws.persistence.repository.CustomItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DeleteCustomItemService {
    private final Logger logger = LoggerFactory.getLogger(DeleteCustomItemService.class);

    @Autowired
    private CustomItemRepository customItemRepository;

    public DeleteCustomItemService() {}

    public ResponseEntity<ResponseDto<CustomItem>> run(
            String customItemId
    ) {
        logger.info("message:Deleting custom item...");

        var customItem = customItemRepository.getCustomItemById(customItemId);
        if (customItem == null)
        {
            logger.error("message:Custom item is not found.");

            var responseDto = new ResponseDto<CustomItem>();
            responseDto.setMessage("Custom item is not found.");

            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }

        customItemRepository.deleteCustomItem(customItem);
        logger.info("message:Custom item is deleted.");

        var responseDto = new ResponseDto<CustomItem>();
        responseDto.setMessage("Custom item is deleted.");
        responseDto.setData(customItem);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}

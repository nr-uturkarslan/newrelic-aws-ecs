package com.newrelic.aws.persistence.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.newrelic.aws.persistence.entity.CustomItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CustomItemRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public CustomItem saveCustomItem(CustomItem customItem) {
        dynamoDBMapper.save(customItem);
        return customItem;
    }

    public CustomItem getCustomItemById(String id) {
        return dynamoDBMapper.load(CustomItem.class, id);
    }

    public void deleteCustomItemById(String id) {
        dynamoDBMapper.delete(dynamoDBMapper.load(CustomItem.class, id));
    }
}

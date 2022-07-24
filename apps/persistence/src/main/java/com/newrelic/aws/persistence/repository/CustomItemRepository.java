package com.newrelic.aws.persistence.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.newrelic.aws.persistence.entity.CustomItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomItemRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public void saveCustomItem(CustomItem customItem) {
        dynamoDBMapper.save(customItem);
    }

    public CustomItem getCustomItemById(String id) {
        return dynamoDBMapper.load(CustomItem.class, id);
    }

    public List<CustomItem> getCustomItems(int limit) {
        var scanExpression = new DynamoDBScanExpression()
                .withLimit(limit);
        return dynamoDBMapper.scan(CustomItem.class, scanExpression);
    }

    public void deleteCustomItem(CustomItem customItem) {
        dynamoDBMapper.delete(customItem);
    }
}

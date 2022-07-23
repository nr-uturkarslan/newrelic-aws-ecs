package com.newrelic.aws.persistence.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
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

    public List<CustomItem> getCustomItems(Integer limit) {
        var queryExpression = new DynamoDBQueryExpression<CustomItem>()
                .withLimit(limit);
        return dynamoDBMapper.query(CustomItem.class, queryExpression);
    }

    public void deleteCustomItem(CustomItem customItem) {
        dynamoDBMapper.delete(customItem);
    }
}

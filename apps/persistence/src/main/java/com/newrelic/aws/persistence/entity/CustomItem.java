package com.newrelic.aws.persistence.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "DynamoDbCustomItem")
public class CustomItem {
    @DynamoDBHashKey
    private String id;

    @DynamoDBAttribute
    private String name;

    @DynamoDBAttribute
    private String description;

    @DynamoDBAttribute
    private String requestTimestamp;

    @DynamoDBAttribute
    private String validationTimestamp;

    @DynamoDBAttribute
    private boolean isValid;
}

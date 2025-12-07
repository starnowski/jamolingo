package com.github.starnowski.jamolingo.select;

import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OdataSelectToMongoProjectParser {
    public static Bson buildProjection(SelectOption selectOption) {
        if (selectOption == null || selectOption.getSelectItems().isEmpty()) {
            return new Document(); // no projection → all fields
        }

        List<String> fields = new ArrayList<>();
        for (SelectItem item : selectOption.getSelectItems()) {
            if (item.isStar()) {
                return new Document(); // * means all fields
            }
            if (!item.getResourcePath().getUriResourceParts()
                    .stream().allMatch(
                            p -> p instanceof UriResourceProperty
                    )) {
                throw new RuntimeException("Invalid select parameter " + item.getResourcePath());
            }
            String propertyName = item.getResourcePath().getUriResourceParts()
                    .stream()
                    .map(p -> ((UriResourceProperty) p).getProperty().getName())
                    .collect(Collectors.joining("."));
            fields.add(propertyName);
        }

        return new Document(Map.ofEntries(fields.stream().distinct().map(field -> Map.entry(field, 1)).toList().toArray(new Map.Entry[0])));
    }
}

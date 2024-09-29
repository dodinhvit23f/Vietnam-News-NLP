package com.news.scanner.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Link {
    String url;
    List<String> categories;
    boolean isSubDomain;
    String domain;
    String baseUrl;
}

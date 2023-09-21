package com.nlp.news.documents;

import java.util.Objects;

import lombok.Builder;
import lombok.Data;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("news")
@Data
@Builder
public class News {

  String id;
  String link;
  String name;
  String content;
  String domain;

  @Override
  public boolean equals(Object o) {
    if (Objects.isNull(o)) {
      return Boolean.FALSE;
    }

    if (o == this) {
      return Boolean.TRUE;
    }

    if (!(o instanceof News)) {
      return Boolean.FALSE;
    }
    News temp = (News) o;

    return getLink().equals(temp.getLink());
  }

}

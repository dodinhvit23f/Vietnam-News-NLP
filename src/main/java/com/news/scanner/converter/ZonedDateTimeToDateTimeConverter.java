package com.news.scanner.converter;

import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;
import java.util.Date;

public class ZonedDateTimeToDateTimeConverter implements Converter<ZonedDateTime, Date> {

    public Date convert(ZonedDateTime source) {
        return source == null ? null : Date.from(source.toInstant());
    }
}

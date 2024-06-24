package com.news.scanner.converter;

import org.springframework.core.convert.converter.Converter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateTimeToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {


    @Override
    public ZonedDateTime convert(Date source) {
        return source == null ? null : ZonedDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault());
    }
}

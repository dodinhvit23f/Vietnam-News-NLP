package com.news.scanner.news;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.chrome.ChromeDriver;
import reactor.core.publisher.Mono;

import java.util.Optional;

@NoArgsConstructor
@Slf4j
public abstract class NewsScanner {
    public static final String HREF = "href";
    public static final String A_TAG = "a";

    abstract String getBaseUrl();
    abstract String getDomain();

    Optional<Document> getDocument(String url, ChromeDriver chromeDriver) {

        try {
            chromeDriver.get(url);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if( chromeDriver.getPageSource().isEmpty()){
            return Optional.empty();
        }

        return Optional.of(Jsoup.parse(chromeDriver.getPageSource()));
    }
}

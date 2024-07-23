package com.news.scanner.news;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.util.ObjectUtils;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@NoArgsConstructor
@Slf4j
public abstract class NewsScanner {
    public static final String HREF = "href";
    public static final String A_TAG = "a";

     Set<String> linkCollection = ConcurrentHashMap.newKeySet();
     Queue<String> queue = new ConcurrentLinkedQueue<>();

    protected void addDocumentCollectionForCrawl(String link) {
        if (linkCollection.add(link)) {
            queue.add(link);
        }
    }

    protected void addDocumentCollection(String link) {
        linkCollection.add(link);
    }

    protected String getQueueUrl() {
        return queue.poll();
    }

    protected  boolean queueEmpty(){
        return queue.isEmpty();
    }

    abstract String getBaseUrl();

    abstract String getDomain();

    abstract void scanWeb();

    Optional<Document> getDocument(String url, ChromeDriver chromeDriver) {

        try {
            chromeDriver.get(url);
            int retryTimes = 1000;
            while (chromeDriver.getTitle().contains("Bad gateway")) {
                retryTimes = retryTimes + 400 + (retryTimes / 100);
                Thread.sleep(retryTimes);
                chromeDriver.get(url);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if (chromeDriver.getPageSource().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Jsoup.parse(chromeDriver.getPageSource()));
    }


}

package com.news.scanner.news;

import com.news.scanner.dto.Link;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor
@Slf4j
public abstract class NewsScanner {
    public static final String HREF = "href";
    public static final String A_TAG = "a";
    protected List<String> nonDocument = List.of("jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp", "svg", "ico", "heif",
            "heic", "ics");

    protected List<String> documentExtension = List.of("txt", "pdf", "xml", "exe", "xls", "xlsx", "xlsm", "xlsb",
            "xltx", "xltm", "docx", "zip", "doc", "pptx");

    Set<String> linkCollection = ConcurrentHashMap.newKeySet();
    Queue<Link> queue = new ConcurrentLinkedQueue<>();

    protected void addDocumentCollectionForCrawl(String link) {
        AtomicBoolean validLink = new AtomicBoolean(link.startsWith(getBaseUrl()));

        AtomicBoolean isSubDoMain = new AtomicBoolean(Boolean.FALSE);
        AtomicReference<String> domain = new AtomicReference<>();

        if (!validLink.get()) {
            getSubDomain().forEach(subDomain -> {
                if (link.startsWith(subDomain) && !isSubDoMain.get()) {
                    isSubDoMain.set(Boolean.TRUE);
                    domain.set(subDomain);
                    validLink.set(Boolean.TRUE);
                }
            });
        }

        if (!linkCollection.add(link)) {
            validLink.set(Boolean.FALSE);
        }

        if (!validLink.get()) {
            return;
        }

        if (isSubDoMain.get()) {
            queue.add(Link.builder()
                    .url(link)
                    .isSubDomain(Boolean.TRUE)
                    .categories(getSubDomainCategories(domain.get()))
                    .domain(domain.get())
                    .build());
            return;
        }

        queue.add(Link.builder()
                .url(link)
                .isSubDomain(Boolean.FALSE)
                .baseUrl(getBaseUrl())
                .build());
    }

    protected void addDocumentCollection(String link) {
        linkCollection.add(link);
    }

    protected Link getQueueUrl() {
        return queue.poll();
    }

    protected boolean queueEmpty() {
        return queue.isEmpty();
    }

    abstract String getBaseUrl();

    abstract String getDomain();

    abstract List<String> getSubDomainCategories(String subDomain);

    abstract void scanByUrl(Link link);


    abstract List<String> getSubDomain();

     public void scanWeb(){
         addDocumentCollectionForCrawl(getBaseUrl());
         getSubDomain().forEach(this::addDocumentCollectionForCrawl);
         while (!queueEmpty()) {
             scanByUrl(getQueueUrl());
         }
    };

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

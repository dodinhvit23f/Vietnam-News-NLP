package com.news.scanner.news;

import com.news.scanner.dto.Link;
import com.news.scanner.entity.News;
import com.news.scanner.repositories.NewsRepository;
import com.news.scanner.utils.Utilization;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
@NoArgsConstructor
public abstract class NewsScanner {

    public static final String HREF = "href";
    public static final String A_TAG = "a";
    protected List<String> nonDocument = List.of("jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp", "svg", "ico", "heif",
            "heic", "ics");

    protected List<String> documentExtension = List.of("txt", "pdf", "xml", "exe", "xls", "xlsx", "xlsm", "xlsb",
            "xltx", "xltm", "docx", "zip", "doc", "pptx");

    Set<String> linkCollection = ConcurrentHashMap.newKeySet();
    Queue<Link> queue = new ConcurrentLinkedQueue<>();

    protected void addDocumentCollectionForCrawl(String link, String domain) {
        AtomicBoolean validLink = new AtomicBoolean(link.startsWith(domain));

        if (!linkCollection.add(link)) {
            validLink.set(Boolean.FALSE);
        }

        if (!validLink.get()) {
            return;
        }

        queue.add(Link.builder()
                .url(link)
                .isSubDomain(getSubDomain().contains(domain))
                .categories(getSubDomainCategories(domain))
                .domain(domain)
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

    abstract List<String> findPageCategories(Document document, String domain);

    abstract String findPageContent(Document document, String domain);

    public void scanWeb() {
        addDocumentCollectionForCrawl(getBaseUrl(), getBaseUrl());
        getSubDomain().forEach(link -> addDocumentCollectionForCrawl(link, link));
        while (!queueEmpty()) {
            scanByUrl(getQueueUrl());
        }
    }

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


    public void saveNews(Document document, Link url, NewsRepository newsRepository) {
        Optional<News> newsOptional = newsRepository.findByUrl(url.getUrl());

        if (newsOptional.isEmpty()) {

            List<String> categories = new ArrayList<>(url.getCategories());
            categories.addAll(findPageCategories(document, url.getDomain()));
            String content = Utilization.splitText(
                    findPageContent(document, url.getDomain()),
                    Utilization.getPunctuationForLanguage());

            News news = News.builder()
                    .title(document.title())
                    .url(url.getUrl())
                    .domain(getDomain())
                    .content(content)
                    .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                    .category(categories)
                    .build();

            newsRepository.save(news);
        }
    }

}

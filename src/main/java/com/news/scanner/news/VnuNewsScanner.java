package com.news.scanner.news;

import com.news.scanner.dto.Link;
import com.news.scanner.entity.News;
import com.news.scanner.repositories.NewsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class VnuNewsScanner extends NewsScanner {
    public static final String Admission = "https://tuyensinh.uet.vnu.edu.vn/";
    ChromeDriver chromeDriver;
    NewsRepository newsRepository;

    @Override
    String getBaseUrl() {
        return "https://uet.vnu.edu.vn/";
    }

    @Override
    String getDomain() {
        return "uet.vnu.edu";
    }

    @Override
    public void scanWeb() {
        addDocumentCollectionForCrawl(getBaseUrl());
        while (!queueEmpty()) {
            scanByUrl(getQueueUrl());
        }
    }

    @Override
    List<String> getSubDomain() {
        return List.of(Admission);
    }

    public void scanByUrl(Link rootLink) {
        // category property"v:title"
        Optional<Document> documentOptional = getDocument(rootLink.getUrl(), chromeDriver);

        documentOptional.ifPresent(document -> {
            Set<String> scanUrlSet = document.select(NewsScanner.A_TAG)
                    .stream()
                    .filter(aTag -> aTag.hasAttr(NewsScanner.HREF))
                    .filter(aTag -> !aTag.attribute(NewsScanner.HREF).getValue().contains("/en"))
                    .map(aTag -> aTag.attribute(NewsScanner.HREF).getValue().strip())
                    .collect(Collectors.toSet());

            scanUrlSet.forEach(scanUrl -> {
                String[] endOfUr = scanUrl.split("\\.");
                if (ObjectUtils.isEmpty(endOfUr) ||
                        nonDocument.contains(endOfUr[endOfUr.length - 1])) {
                    addDocumentCollection(scanUrl);
                    return;
                }

                if (documentExtension.contains(endOfUr[endOfUr.length - 1])) {
                    String categoryString = document.select(".breadcrumbs").text();
                    if (ObjectUtils.isEmpty(categoryString)) {
                        return;
                    }
                    String[] categories = categoryString.split("\\\\");
                    saveNews(documentOptional.get(), scanUrl, Arrays.stream(categories).map(String::strip).collect(Collectors.toList()));
                    addDocumentCollection(scanUrl);
                    return;
                }

                addDocumentCollectionForCrawl(scanUrl);
            });
            saveNews(document, rootLink);
        });


    }

    public void saveNews(Document document, Link link) {
        News news = null;
        if (!link.isSubDomain()) {
            news = getRootDomain(document, link);
        } else {
            switch (link.getDomain()){
                case Admission:
                    news = getAdmissionsDomain(document, link);
                    break;
            }
        }

        if (Objects.isNull(news)) {
            return;
        }

        newsRepository.save(news);
    }

    public void saveNews(Document document, String url, List<String> categories) {
        Optional<News> newsOptional = newsRepository.findByUrl(url);
        if (newsOptional.isEmpty()) {
            News news = News.builder()
                    .title(document.title())
                    .url(url)
                    .domain(getDomain())
                    .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                    .category(categories)
                    .build();
            newsRepository.save(news);
        }
    }

    public News getRootDomain(Document document, Link link) {
        String content = document.select("#content").text();
        String categoryString = document.select(".breadcrumbs").text();
        if (ObjectUtils.isEmpty(categoryString)) {
            return null;
        }

        String[] categories = categoryString.split("\\\\");

        return newsRepository.findByUrl(link.getUrl()).orElse(News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(Arrays.stream(categories).map(String::toLowerCase).toList())
                .build());
    }

    public News getAdmissionsDomain(Document document, Link link) {

        String content = document.select("#resume-timeline").text();
        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }
}

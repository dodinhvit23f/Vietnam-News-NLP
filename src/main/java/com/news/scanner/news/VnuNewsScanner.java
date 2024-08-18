package com.news.scanner.news;

import com.news.scanner.dto.Link;
import com.news.scanner.entity.News;
import com.news.scanner.repositories.NewsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
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
    public static final String ADMISSION = "https://tuyensinh.uet.vnu.edu.vn/";
    public static final String ELECTRIC = "https://fet.uet.vnu.edu.vn/";
    public static final String IT = "https://www.fit.uet.vnu.edu.vn/";
    public static final String NANO_TECH = "http://fepn.uet.vnu.edu.vn/";
    public static final String AUTOMATIC = "https://fema.uet.vnu.edu.vn/";
    public static final String AGRICULTURE = "http://fat.uet.vnu.edu.vn/";
    public static final String CONSTRUCTION = "https://fce.uet.vnu.edu.vn/";
    public static final String SPACE = "https://sae.uet.vnu.edu.vn/";
    public static final String TECHNOLOGY = "https://avitech.uet.vnu.edu.vn/";
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
    List<String> getSubDomainCategories(String subDomain) {
        Map<String, List<String>> categories = new HashMap<>();
        categories.put(ADMISSION, List.of("Tuyển Sinh"));
        categories.put(ELECTRIC, List.of("điện"));
        categories.put(IT, List.of("công nghệ thông tin"));
        categories.put(NANO_TECH, List.of("vật lý kỹ thuật & công nghệ nano"));
        categories.put(AUTOMATIC, List.of("cơ học kỹ thuật và tự động hoá"));
        categories.put(AGRICULTURE, List.of("công nghệ nông nghiệp"));
        categories.put(CONSTRUCTION, List.of("công nghệ xây dựng và giao thông"));
        categories.put(SPACE, List.of("công nghệ hàng không và vũ trụ"));
        categories.put(TECHNOLOGY, List.of("tiên tiến về kỹ thuật công nghệ"));

        return categories.get(subDomain);
    }

    @Override
    List<String> getSubDomain() {
        return List.of(ADMISSION, ELECTRIC, IT, NANO_TECH, AUTOMATIC, CONSTRUCTION, SPACE, TECHNOLOGY, AGRICULTURE);
    }

    @Override
    public void scanWeb() {
        getSubDomain().forEach(sub -> addDocumentCollectionForCrawl(sub));;
        while (!queueEmpty()) {
            scanByUrl(getQueueUrl());
        }
    }

    public void scanByUrl(Link rootLink) {
        // category property"v:title"
        Optional<Document> documentOptional = getDocument(rootLink.getUrl(), chromeDriver);

        documentOptional.ifPresent(document -> {
            Set<String> scanUrlSet = chromeDriver.findElements(By.tagName(NewsScanner.A_TAG))
                    .stream()
                    .filter(aTag -> !ObjectUtils.isEmpty(aTag.getAttribute(NewsScanner.HREF)))
                    .filter(aTag -> !aTag.getAttribute(NewsScanner.HREF).contains("/en"))
                    .map(aTag -> aTag.getAttribute(NewsScanner.HREF).strip()
                            .replace("#","")
                            .replace("/respond", "/"))
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
            switch (link.getDomain()) {
                case ADMISSION:
                    news = getAdmissionsDomain(document, link);
                    break;
                case IT:
                    news = getItDomain(document, link);
                    break;
                case AGRICULTURE:
                    news = getAgricultureDomain(document, link);
                    break;
                case AUTOMATIC:
                    news = getAutomaticDomain(document, link);
                    break;
                case CONSTRUCTION:
                    news = getConstructionDomain(document, link);
                    break;
                case SPACE:
                    news = getSpaceDomain(document, link);
                    break;
                case TECHNOLOGY:
                    news = getTechnologyDomain(document, link);
                    break;
                case NANO_TECH:
                    news = getNaoTechDomain(document, link);
                    break;
                case ELECTRIC:
                    news = getElectricDomain(document, link);
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

        String content = document.select("#page-wrapper").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }

    public News getElectricDomain(Document document, Link link) {

        String content = document.select("#td-outer-wrap").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }

    public News getItDomain(Document document, Link link) {

        String content = document.select("#singles").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }

    public News getNaoTechDomain(Document document, Link link) {

        String content = document.select("#module23").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }

    public News getAutomaticDomain(Document document, Link link) {

        String content = document.select("#primary").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }

    public News getAgricultureDomain(Document document, Link link) {

        String content = document.select(".row.row-page-container").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }

    public News getConstructionDomain(Document document, Link link) {

        String content = document.select("#main").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }

    public News getSpaceDomain(Document document, Link link) {

        String content = document.select(".subpage").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        return News.builder()
                .title(document.title())
                .url(link.getUrl())
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .category(link.getCategories())
                .build();
    }

    public News getTechnologyDomain(Document document, Link link) {

        String content = document.select("#wrapper").text();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

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

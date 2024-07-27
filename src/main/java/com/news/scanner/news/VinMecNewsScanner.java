/*
package com.news.scanner.news;

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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import  java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class VinMecNewsScanner extends NewsScanner {
    public static final String PAGE = "?page=";
    ChromeDriver chromeDriver;
    NewsRepository newsRepository;


    @Override
    String getBaseUrl() {
        return "https://www.vinmec.com";
    }


    @Override
    String getDomain() {
        return "vinmec";
    }

    public void scanWeb() {
        String url = getBaseUrl().concat("/vi/");
        linkCollection.add(url);
        queue.add(url);
        while (!queue.isEmpty()){
               scanByUrl(queue.poll());
        }
    }

    @Override
    List<String> getSubDomain() {
        return List.of();
    }

    public void scanByUrl(String url) {
        String[] endOfUr = url.split("\\.");

        if (ObjectUtils.isEmpty(endOfUr) ||
                List.of("txt", "pdf", "xml", "exe", "xls", "xlsx", "xlsm", "xlsb", "xltx", "xltm",
                        "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp", "svg", "ico", "heif",
                        "heic").contains(endOfUr[endOfUr.length - 1])) {
            return;
        }

        String cutUrl = url.split("\\?")[0];
        if (newsRepository.findByUrl(cutUrl).isPresent()) {
            return;
        }

        log.info("scanning url: {}", url);
        Optional<Document> documentOptional = getDocument(url, chromeDriver);
        if (documentOptional.isEmpty()) {
            return;
        }

        Document document = documentOptional.get();

        Optional<News> news = newsRepository.findByUrl(url);
        if (news.isEmpty()) {
            saveNews(document, getVinMecUrl(chromeDriver.getCurrentUrl()));
        }


        Set<String> scanUrlSet = document.select(NewsScanner.A_TAG)
                .stream()
                .filter(aTag -> aTag.hasAttr(NewsScanner.HREF))
                .filter(aTag -> !aTag.attribute(NewsScanner.HREF).getValue().contains("/en/"))
                .filter(aTag -> aTag.attribute(NewsScanner.HREF).getValue().startsWith(PAGE))
                .filter(aTag -> !aTag.attribute(NewsScanner.HREF).getValue().startsWith(PAGE.concat("1")))
                .map(aTag -> aTag.attribute(NewsScanner.HREF).getValue().strip())
                .map(path -> {
                    if (!path.contains(PAGE)) {
                        return path.concat(path);
                    }

                    String[] currentUrlPath = url.split("\\?page=");
                    String[] urlPath = path.split("\\?page=");

                    if (currentUrlPath.length == 2 &&
                            urlPath.length == 2 &&
                            Integer.parseInt(currentUrlPath[1]) < Integer.parseInt(urlPath[1])) {
                        return currentUrlPath[0].concat(path);
                    }

                    return null;
                })
                .filter(link -> !ObjectUtils.isEmpty(link))
                .collect(Collectors.toSet());

        scanUrlSet.addAll(document.select(NewsScanner.A_TAG)
                .stream()
                .filter(aTag -> aTag.hasAttr(NewsScanner.HREF))
                .filter(aTag -> aTag.attribute(NewsScanner.HREF).getValue().startsWith("/"))
                .filter(aTag -> !aTag.attribute(NewsScanner.HREF).getValue().contains("/en/"))
                .map(aTag -> aTag.attribute(NewsScanner.HREF).getValue().strip())
                .filter(path -> !ObjectUtils.isEmpty(path) && path.length() > 1)
                .map(path -> getVinMecUrl(getBaseUrl().concat(path)))
                .collect(Collectors.toSet()));

        scanUrlSet.addAll(document.select(NewsScanner.A_TAG)
                .stream()
                .filter(aTag -> aTag.hasAttr(NewsScanner.HREF))
                .filter(aTag -> aTag.attribute(NewsScanner.HREF).getValue().startsWith("https://www.vinmec.com"))
                .filter(aTag -> !aTag.attribute(NewsScanner.HREF).getValue().contains("/en/"))
                .map(aTag -> getVinMecUrl(aTag.attribute(NewsScanner.HREF).getValue()))
                .collect(Collectors.toSet()));

        scanUrlSet.stream()
                .filter(link -> !queue.contains(link))
                .filter(link -> !linkCollection.contains(link))
                .forEach(link -> {
                    queue.add(link.);
                    linkCollection.add(link);
                });
        linkCollection.add(chromeDriver.getCurrentUrl());
    }

    public News saveNews(Document document, String url) {

        String content = document.select(".block-content.cms.pageview-highest").text();

        if (ObjectUtils.isEmpty(content)) {
            content = document.select(".content.col-xs-12.col-md-8.no-paddings-sm").text();
        }

        if (ObjectUtils.isEmpty(content)) {
            content = document.select("#profile").text();
        }

        if(ObjectUtils.isEmpty(content)){
            content = document.select(".col-sm-12.col-md-8").text();
        }

        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        News news = News.builder()
                .title(document.title())
                .url(url)
                .domain(getDomain())
                .content(content)
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();

        return newsRepository.save(news);
    }

    private static String getVinMecUrl(String url) {
        return url.substring(0, url.lastIndexOf('/') + 1);
    }


}
*/

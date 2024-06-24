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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class VinMecNewsScanner extends NewsScanner {
    ChromeDriver chromeDriver;
    NewsRepository newsRepository;
    Set<String> scanningUrl = new HashSet<>();
    Set<String> queue = ConcurrentHashMap.newKeySet();

    @Override
    String getBaseUrl() {
        return "https://www.vinmec.com";
    }


    @Override
    String getDomain() {
        return "vinmec";
    }

    public void scanWeb() {
        scanByUrl(getBaseUrl().concat("/vi/"));
        queue.forEach(this::scanByUrl);
        //scanByUrl("https://www.vinmec.com/vi/tin-tuc/");
    }

    public void scanByUrl(String url) {
        String[] endOfUr = url.split("\\.");

        if (ObjectUtils.isEmpty(endOfUr) ||
                List.of("txt", "pdf", "xml", "exe", "xls", "xlsx", "xlsm", "xlsb", "xltx", "xltm",
                        "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp", "svg", "ico", "heif",
                        "heic").contains(endOfUr[endOfUr.length - 1])) {
            return;
        }

        if (scanningUrl.contains(url)) {
            return;
        }
        scanningUrl.add(url);
        log.info("scanning url: {}", url);
        Optional<Document> documentOptional = getDocument(url, chromeDriver);
        if (documentOptional.isEmpty()) {
            return;
        }

        Document document = documentOptional.get();

        Optional<News> news = newsRepository.findByUrl(url);
        if (news.isEmpty()) {
            saveNews(document, url);
        }


        Set<String> scanUrlSet = document.select(NewsScanner.A_TAG)
                .stream()
                .filter(aTag -> aTag.hasAttr(NewsScanner.HREF))
                .filter(aTag -> aTag.attribute(NewsScanner.HREF).getValue().startsWith("/"))
                .filter(aTag -> !aTag.attribute(NewsScanner.HREF).getValue().contains("/en/"))
                .map(aTag -> aTag.attribute(NewsScanner.HREF).getValue().strip())
                .filter(path -> !ObjectUtils.isEmpty(path) && path.length() > 1)
                .map(path -> getBaseUrl().concat(path))
                .filter(newUrl -> !scanningUrl.contains(newUrl))
                .collect(Collectors.toSet());


        scanUrlSet.addAll(document.select(NewsScanner.A_TAG)
                .stream()
                .filter(aTag -> aTag.hasAttr(NewsScanner.HREF))
                .filter(aTag -> aTag.attribute(NewsScanner.HREF).getValue().startsWith("https://www.vinmec.com"))
                .filter(aTag -> !aTag.attribute(NewsScanner.HREF).getValue().contains("/en/"))
                .map(aTag -> aTag.attribute(NewsScanner.HREF).getValue())
                .filter(newUrl -> !scanningUrl.contains(newUrl))
                .collect(Collectors.toSet()));

        scanUrlSet.addAll(document.select(NewsScanner.A_TAG)
                .stream()
                .filter(aTag -> aTag.hasAttr(NewsScanner.HREF))
                .filter(aTag -> !aTag.attribute(NewsScanner.HREF).getValue().contains("/en/"))
                .filter(aTag -> aTag.attribute(NewsScanner.HREF).getValue().startsWith("?"))
                .map(aTag -> aTag.attribute(NewsScanner.HREF).getValue().strip())
                .map(url::concat)
                .filter(newUrl -> !scanningUrl.contains(newUrl))
                .collect(Collectors.toSet()));

        queue.addAll(scanUrlSet);
    }

    public News saveNews(Document document, String url) {
        if (url.contains("?page")) {
            System.out.println();
        }
        News news = News.builder()
                .title(document.title())
                .url(url)
                .domain(getDomain())
                .content(document.select("#main div").text())
                .createAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();

        return newsRepository.save(news);
    }


}

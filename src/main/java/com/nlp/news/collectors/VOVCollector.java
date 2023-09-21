package com.nlp.news.collectors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.nlp.news.constant.HTMLConstant;
import com.nlp.news.documents.News;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

import static com.nlp.news.constant.HTMLConstant.A_TAG;
import static com.nlp.news.constant.HTMLConstant.HREF;

@Service
public class VOVCollector extends Collector {

  @Override
  Map<String, String> getNewsType(Document document, WebDriver webDriver) {
    Map<String, String> typeLink = new ConcurrentHashMap<>();
    Element body = document.body();
    Elements subMenus = body.getElementsByClass("sub-menu");
    Elements types = subMenus.get(0).getElementsByClass("widget-title");

    types.stream()
        .map(h5 -> h5.getElementsByTag(A_TAG))
        .filter(elements -> !ObjectUtils.isEmpty(elements))
        .map(aTags -> aTags.get(0))
        .forEach(aTag -> typeLink.putIfAbsent(aTag.text(),
            String.join("", getWebUrl(), aTag.attr(HREF))));

    return typeLink;
  }

  @Override
  String getWebName() {
    return "vov";
  }

  @Override
  String getWebUrl() {
    return "https://vov.vn";
  }

  @Override
  Optional<String> getNextPage(Document document, WebDriver webDriver) {
    Elements pageItems = document.getElementsByClass("page-link");

    Element nextPage = null;
    Boolean breakLoop = Boolean.FALSE;

    for (int i = 0; i < pageItems.size(); i++) {
      if (pageItems.get(i).classNames().contains("active")) {
        breakLoop = Boolean.TRUE;
        continue;
      }

      if (breakLoop) {
        nextPage = pageItems.get(i);
        break;
      }
    }

    if (Objects.nonNull(nextPage) &&
        NumberUtils.isCreatable(nextPage.text().replace("Trang", "").trim())) {
      return Optional.of(String.join("", getWebUrl(), nextPage.attr(HREF)));
    }

    return Optional.empty();
  }

  @Override
  List<News> getPageNew(Document doc, String domain, WebDriver webDriver) {
    Set<Element> elementNews = doc.getElementsByClass("views-element-container")
        .stream()
        .map(row -> row.getElementsByClass("taxonomy-content"))
        .filter(row -> !ObjectUtils.isEmpty(row))
        .flatMap(List::stream)
        .collect(Collectors.toSet());

    if (elementNews.isEmpty()) {
      return Collections.emptyList();
    }

    return elementNews
        .stream()
        .map(element -> News.builder()
              .name(element.getElementsByClass("card-title").get(0).text())
              .link(String.join("",
                  getWebUrl(),
                  element.getElementsByClass("vovvn-title position-relative").get(0).attr(HREF)))
              .content("")
              .domain(domain)
              .build())
        .toList();
  }

  @Override
  void getPageContent(List<News> news, WebDriver webDriver) {
    news.forEach(
       document ->{
         webDriver.get(document.getLink());

         Document doc = Jsoup.parse(webDriver.getPageSource());
         Elements elements = doc.body().getElementsByClass("detail--normal");

         if(ObjectUtils.isEmpty(elements)){
           elements = doc.body().getElementsByClass("video__summary");
         }

         String content =  elements.get(0).text()
                 .replace("\s+", "\s")
                 .replace("\r\n", "\n");

         document.setContent(content);
       }
    );
  }
}

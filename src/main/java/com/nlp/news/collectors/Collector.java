package com.nlp.news.collectors;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nlp.news.documents.News;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;

public abstract class Collector {

  /**
   * Using Jsoup to get HTML tag and get news type
   *
   * @param webDriver
   * @return name type and type url
   */
  abstract Map<String, String> getNewsType(Document doc, WebDriver webDriver);

  abstract String getWebName();

  abstract String getWebUrl();

  abstract Optional<String> getNextPage(Document document, WebDriver webDriver);

  abstract List<News> getPageNew(Document doc, String domain, WebDriver webDriver);

  abstract void getPageContent(List<News> news, WebDriver webDriver);

  public void run(WebDriver webDriver) {
    webDriver.get(getWebUrl());
    Document doc = Jsoup.parse(webDriver.getPageSource());
    Map<String, String> typeLink = getNewsType(doc, webDriver);

    typeLink.forEach((type, link) -> {
      webDriver.get(link);

      while (Boolean.TRUE) {
        Document newsPage = Jsoup.parse(webDriver.getPageSource());

        Optional<String> nextPage = getNextPage(newsPage, webDriver);

        List<News> news = getPageNew(newsPage, getWebName(), webDriver);

        getPageContent(news, webDriver);

        if(nextPage.isEmpty()){
          break;
        }

        webDriver.get(nextPage.get());
      }
    });
  }

}

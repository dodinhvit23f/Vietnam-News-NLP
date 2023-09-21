package com.nlp.news.schedules;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nlp.news.collectors.Collector;
import org.openqa.selenium.WebDriver;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleCronCrawler {

  final Collector vovCollector;
  final WebDriver webDriver;

  @Scheduled(initialDelay = 1000, fixedDelay = 1)
  void crawlWebJob(){
    vovCollector.run(webDriver);
  }

}

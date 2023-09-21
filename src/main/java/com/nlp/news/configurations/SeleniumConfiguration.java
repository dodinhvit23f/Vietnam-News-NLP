package com.nlp.news.configurations;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

@Configuration
public class SeleniumConfiguration {

  @Value("${chromelium.driver.path}")
  String webDriverPath;

  @Value("${chromelium.extension.path}")
  String extensionPath;

  @Value("${chromelium.adblock.path}")
  String adBlockPath;

  @Bean
  WebDriver getChromeDriver() throws FileNotFoundException {

    ChromeOptions options = getChromeOptions();

    ChromeDriverService chromeDriverService = new ChromeDriverService.Builder()
        .usingDriverExecutable(ResourceUtils.getFile(webDriverPath))
        .usingAnyFreePort()
        .build();

    return new ChromeDriver(chromeDriverService, options);
  }

  private ChromeOptions getChromeOptions() {
    ChromeOptions options = new ChromeOptions();

    //options.addArguments("--headless=chrome");
    options.addArguments("--ignore-ssl-errors=yes");
    options.addArguments("--ignore-certificate-errors");
    options.addArguments("--lang=vi");

    options.addArguments("--disable-extensions");
    options.addArguments("--disable-security");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--allow-running-insecure-content");
    options.addArguments("--allowed-ips");
    options.addArguments("accept-language=vi");
    options.addArguments("--disable-blink-features=AutomationControlled");
    options.addExtensions(new File(adBlockPath));

    options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
    return options;
  }

}

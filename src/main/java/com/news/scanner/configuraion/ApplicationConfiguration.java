package com.news.scanner.configuraion;

import com.news.scanner.converter.DateTimeToZonedDateTimeConverter;
import com.news.scanner.converter.ZonedDateTimeToDateTimeConverter;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class ApplicationConfiguration {

    @Bean
    WebClient webClient(HttpClient httpClient) {
        final int size = 16 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean
    HttpClient getHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .followRedirect(Boolean.TRUE)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));
    }

    @Value("${application.chrome}")
    String pathToChrome;

    @Bean
    ChromeDriverService webDriverBrowser() throws FileNotFoundException {
        return new ChromeDriverService.Builder()
                .usingDriverExecutable(ResourceUtils.getFile(pathToChrome))
                .build();
    }

    @Bean
    ChromeOptions geChromeOptions() {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--ignore-ssl-errors=yes");
        options.addArguments("--ignore-certificate-errors");
        //options.add_experimental_option("excludeSwitches", ["enable-automation"]);
        //  options.add_experimental_option('useAutomationExtension', False);
        // đặt ngôn ngữ việt công cụ
        options.addArguments("--lang=vi");
        options.addArguments("--disable-web-security");
        // tạo nơi lưu trữ thông tin user-cookie
        //options.addArguments("user-data-dir=C:\\User\\Admin\\AppData\\Google\\Chrome\\User Data\\")
        // loại bỏ các bảo vệ của chrome, và thông báo software auto ....
        options.addArguments("--disable-extensions");
        // options.addArguments("--disable-security");
        options.addArguments("--no-sandbox");
        //// options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--allow-running-insecure-content");
        // đặt định dạng tiếng việt
        options.addArguments("accept-language=vi");
        // tránh nhận dạnh của các trang web là trình duyệt tự động.
        options.addArguments("--disable-blink-features=AutomationControlled");
        //options.add_experimental_option("excludeSwitches", ["enable-automation"])
        //options.add_experimental_option('useAutomationExtension', False);


        return options;
    }

    @Bean
    ChromeDriver chromeDriver(ChromeDriverService service, ChromeOptions chromeOptions) {
        ChromeDriver driver = new ChromeDriver(service, chromeOptions);
        driver.manage().window().maximize();
        driver.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        return driver;
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new DateTimeToZonedDateTimeConverter());
        converters.add(new ZonedDateTimeToDateTimeConverter());
        return new MongoCustomConversions(converters);
    }

}

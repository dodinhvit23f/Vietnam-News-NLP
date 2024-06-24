package com.news.scanner.job;

import com.news.scanner.news.VinMecNewsScanner;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Slf4j
public class VinMecJob {

    VinMecNewsScanner vinMecNes;

    @Scheduled(initialDelay = 1, fixedDelay = 1000000L)
    //@Scheduled(cron = "0 0 1 * * *")
    public void runJob(){
        log.info("Start scan vinmec: {}", Date.from(Instant.now()));
        vinMecNes.scanWeb();
        log.info("End scan vinmec: {}", Date.from(Instant.now()));
    }
}

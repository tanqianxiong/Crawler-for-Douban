package tango.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import tango.crawler.service.CrawlerService;
import tango.crawler.util.Constant;

import java.util.Random;

@Service
public class Entrance {
    @Autowired
    private CrawlerService crawlerService;

    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("app-context.xml");
        System.out.println("Context loaded.");

        Entrance entrance = ac.getBean(Entrance.class);

        System.out.println("Start");

        //因为想「每次抓取」作为「一个事务」，故循环放到service外面；
        for (int i = 0; i < Constant.MAX_COUNT; i++) {
            try {
                sleepAwhile();
                entrance.crawlerService.crawlOnePage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("End");
    }

    public static void sleepAwhile() throws InterruptedException {
        long a = (new Random().nextInt(2) + 1) * 1000;
        System.out.println("Sleep " + a + " ms");
        Thread.sleep(a);
    }
}

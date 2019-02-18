package com.spread;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class,
                properties = {
        "spring.profiles.active=test",
        "log.file.path=/logs",
    })
public class AppTests {

    @Test
    public void contextLoads() {
    }

}

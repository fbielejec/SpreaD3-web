package com.spread;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class
                // properties = {
                //      "spread.vis.location=/home/filip/spread-vis",
                //      "storage.location=/home/filip/spread-storage"
                // }
                )
public class AppTests {

    @Test
    public void contextLoads() {
    }

}

package com.spread;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class,
                properties = {
                    // "spread.vis.location=/home/filip/spread-vis",
                    // "storage.location=/home/filip/spread-storage"
                })
public class AppTests {

    // @Value("${spread.vis.location}")
    // private String visualizationLocation;

    // @Autowired
    // private VisualizationService visualizationService;

    // @Autowired
    // private StorageService storageService;


    @Test
    public void contextLoads() {
        // Mockito.doNothing().when(visualizationService).init(visualizationLocation);
    }

}

package com.spread;

import com.spread.services.visualization.VisualizationService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class,
                properties = {
                    // "spread.vis.location=/home/filip/spread-vis",
                    "storage.location=/home/filip/spread-storage"
                })
public class AppTests {

    @Value("${spread.vis.location}")
    private String visualizationLocation;

    // @Autowired
    @MockBean
    private VisualizationService visualizationService;

    // @Bean
    // @Primary
    // public VisualizationService visualizationServiceTest() {
    //     return Mockito.mock(VisualizationService.class);
    // }

    @Test
    public void contextLoads() {
        // Mockito.doNothing().when(visualizationService).init(visualizationLocation);
    }

}

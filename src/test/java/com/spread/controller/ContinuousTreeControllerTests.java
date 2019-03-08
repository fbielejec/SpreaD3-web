package com.spread.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.spread.TestUtils;
import com.spread.domain.ContinuousTreeModelEntity;
import com.spread.domain.IModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import junit.framework.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ContinuousTreeControllerTests {

    @Autowired
    private WebApplicationContext webContext;

    private MockMvc mockMvc;

    @Before
    public void setupMockMvc() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext) //
            .build();
    }

    @Test
    public void testContinuousTreeController() throws UnsupportedEncodingException, Exception {

        String token = getToken();

        postTree(token);

        putGeojson(token);

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/hpd-level").header("Authorization", "Bearer " + token)
                        .param("hpd-level", "1.1")).andExpect(status().isBadRequest());

        String hpdLevel = TestUtils.expectedHpdLevels.iterator().next();

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/hpd-level").header("Authorization", "Bearer " + token)
                        .param("value", hpdLevel)).andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/coordinates/y")
                        .header("Authorization", "Bearer " + token).param("value", TestUtils.yCoordinate))
            .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/coordinates/x")
                        .header("Authorization", "Bearer " + token).param("value", TestUtils.xCoordinate))
            .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/external-annotations")
                        .header("Authorization", "Bearer " + token).param("value", "true"))
            .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/timescale-multiplier")
                        .header("Authorization", "Bearer " + token).param("value", "-1.0"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/timescale-multiplier")
                        .header("Authorization", "Bearer " + token).param("value", "1.0"))
            .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/mrsd").header("Authorization", "Bearer " + token)
                        .param("value", "2019/02/12")).andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/output").header("Authorization", "Bearer " + token))
            .andExpect(status().isAccepted());

        // poll the status until done
        String status = Stream
            .iterate("", s -> getStatus(token))
            .filter(s -> s.equalsIgnoreCase(IModel.Status.OUTPUT_READY.toString()))
            .findFirst()
            .get();

        Assert.assertEquals(status, IModel.Status.OUTPUT_READY.toString());

        mockMvc.perform(MockMvcRequestBuilders.put("/continuous/ipfs").header("Authorization", "Bearer " + token))
            .andExpect(status().isAccepted());

        // poll the status until done
        status = Stream
            .iterate("", s -> getStatus(token))
            .filter(s -> s.equalsIgnoreCase(IModel.Status.IPFS_HASH_READY.toString()))
            .findFirst()
            .get();

        String resp = mockMvc.perform(MockMvcRequestBuilders.get("/continuous/model")
                                      .header("Authorization", "Bearer " + token)
                                      .contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        ContinuousTreeModelEntity model = new GsonBuilder().create().fromJson(resp, ContinuousTreeModelEntity.class);

        Assert.assertNotNull(model.getIpfsHash());
    }

    private String getToken() throws UnsupportedEncodingException, Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/token")).andReturn().getResponse()
            .getContentAsString();
        return new JsonParser().parse(response).getAsJsonObject().get("token").getAsString();
    }

    private String getStatus (String token)  {
        try {
            String response = mockMvc.perform(MockMvcRequestBuilders.get("/continuous/status")
                                              .header("Authorization", "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            return new JsonParser().parse(response).getAsJsonObject().get("status").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void postTree(String token) throws Exception {
        String filename = "continuous/speciesDiffusion.MCC.tre";
        File treefile = new File(getClass().getClassLoader().getResource(filename).getFile());

        Path path = Paths.get(treefile.getAbsolutePath());
        String name = "file";
        String originalFileName = treefile.getName();
        String contentType = "multipart/form-data";
        byte[] content = Files.readAllBytes(path);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/continuous/tree")
                        .file(new MockMultipartFile(name, originalFileName, contentType, content))
                        .header("Authorization", "Bearer " + token)).andExpect(status().isCreated());
    }

    // private void getAttributes(String token) throws Exception {
    //     String response = mockMvc.perform(MockMvcRequestBuilders.get("/continuous/attributes").header("Authorization", "Bearer " + token))
    //         .andReturn().getResponse().getContentAsString();

    //     Set<String> attributes = TestUtils.jsonArrayToSet(response);
    //     assertEquals(TestUtils.expectedAttributes, attributes);
    // }

    // private void getHpdLevels(String token) throws UnsupportedEncodingException, Exception {
    //     String response = mockMvc.perform(MockMvcRequestBuilders.get("/continuous/hpd-levels").header("Authorization", "Bearer " + token))
    //         .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    //     Set<String> hpdLevels = TestUtils.jsonArrayToSet(response);
    //     assertEquals(TestUtils.expectedHpdLevels, hpdLevels);
    // }

    private void putGeojson(String token) throws Exception {
        String filename = "geojson/subregion_Australia_and_New_Zealand_subunits.geojson";
        File geojsonfile = new File(getClass().getClassLoader().getResource(filename).getFile());

        Path path = Paths.get(geojsonfile.getAbsolutePath());
        String name = "file";
        String originalFileName = geojsonfile.getName();
        String contentType = "multipart/form-data";
        byte[] content = Files.readAllBytes(path);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/continuous/geojson")
                        .file(new MockMultipartFile(name, originalFileName, contentType, content))
                        .header("Authorization", "Bearer " + token)).andExpect(status().isCreated());
    }

}

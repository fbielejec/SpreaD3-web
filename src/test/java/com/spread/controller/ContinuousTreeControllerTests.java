// package com.spread.controller;

// import static org.junit.Assert.assertEquals;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import java.io.File;
// import java.io.UnsupportedEncodingException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.Set;

// import com.google.gson.JsonParser;
// import com.spread.utils.TestUtils;

// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.test.context.junit4.SpringRunner;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.web.context.WebApplicationContext;

// @RunWith(SpringRunner.class)
// @SpringBootTest
// public class ContinuousTreeControllerTests {

//         @Autowired
//         private WebApplicationContext webContext;

//         private MockMvc mockMvc;

//         @Before
//         public void setupMockMvc() throws Exception {
//                 mockMvc = MockMvcBuilders.webAppContextSetup(webContext) //
//                                 .build();
//         }

//         @Test
//         public void testContinuousTreeController() throws UnsupportedEncodingException, Exception {

//                 String response = mockMvc.perform(MockMvcRequestBuilders.get("/token")).andReturn().getResponse()
//                                 .getContentAsString();

//                 String token = new JsonParser().parse(response).getAsJsonObject().get("token").getAsString();

//                 System.out.println(token);

//                 // postContinuousTree(token);

//                 // getAttributes(token);

//                 // getHpdLevels(token);

//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/hpd-level").header("Authorization", "Bearer " + token)
//                 //                 .param("hpd-level", "1.1")).andExpect(status().isBadRequest());

//                 // String hpdLevel = String.valueOf(Double.valueOf(TestUtils.expectedHpdLevels.iterator().next()) / 100);
//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/hpd-level").header("Authorization", "Bearer " + token)
//                 //                 .param("hpd-level", hpdLevel)).andExpect(status().isOk());

//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/coordinates/y")
//                 //                 .header("Authorization", "Bearer " + token).param("attribute", TestUtils.yCoordinate))
//                 //                 .andExpect(status().isOk());

//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/coordinates/x")
//                 //                 .header("Authorization", "Bearer " + token).param("attribute", TestUtils.xCoordinate))
//                 //                 .andExpect(status().isOk());

//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/external-annotations")
//                 //                 .header("Authorization", "Bearer " + token).param("has-external-annotations", "true"))
//                 //                 .andExpect(status().isOk());

//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/timescale-multiplier")
//                 //                 .header("Authorization", "Bearer " + token).param("timescale-multiplier", "-1.0"))
//                 //                 .andExpect(status().isBadRequest());

//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/timescale-multiplier")
//                 //                 .header("Authorization", "Bearer " + token).param("timescale-multiplier", "1.0"))
//                 //                 .andExpect(status().isOk());

//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/mrsd").header("Authorization", "Bearer " + token)
//                 //                 .param("mrsd", "2017/04/06")).andExpect(status().isBadRequest());

//                 // mockMvc.perform(MockMvcRequestBuilders.post("/continuous/mrsd").header("Authorization", "Bearer " + token)
//                 //                 .param("mrsd", "2017-04-06")).andExpect(status().isOk());

//                 // uploadGeoJson(token);

//                 // mockMvc.perform(MockMvcRequestBuilders.get("/continuous/output").header("Authorization", "Bearer " + token))
//                 //                 .andExpect(status().isOk());

//                 // TODO: assert json can be retreived from DB by session-id

//         }

//         public void postContinuousTree(String token) throws Exception {
//                 String filename = "continuous/speciesDiffusion.MCC.tre";
//                 File treefile = new File(getClass().getClassLoader().getResource(filename).getFile());

//                 Path path = Paths.get(treefile.getAbsolutePath());
//                 String name = "treefile";
//                 String originalFileName = treefile.getName();
//                 String contentType = "text/plain";
//                 byte[] content = Files.readAllBytes(path);

//                 mockMvc.perform(MockMvcRequestBuilders.fileUpload("/continuous/tree")
//                                 .file(new MockMultipartFile(name, originalFileName, contentType, content))
//                                 .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
//         }

//         public void getAttributes(String token) throws Exception {
//                 String response = mockMvc
//                                 .perform(
//                                                 MockMvcRequestBuilders.get("/continuous/attributes").header("Authorization", "Bearer " + token))
//                                 .andReturn().getResponse().getContentAsString();

//                 Set<String> attributes = TestUtils.jsonArrayToSet(response);
//                 assertEquals(TestUtils.expectedAttributes, attributes);
//         }

//         private void getHpdLevels(String token) throws UnsupportedEncodingException, Exception {
//                 String response = mockMvc
//                                 .perform(
//                                                 MockMvcRequestBuilders.get("/continuous/hpd-levels").header("Authorization", "Bearer " + token))
//                                 .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

//                 Set<String> hpdLevels = TestUtils.jsonArrayToSet(response);
//                 assertEquals(TestUtils.expectedHpdLevels, hpdLevels);
//         }

//         private void uploadGeoJson(String token) throws Exception {
//                 String filename = "geojson/subregion_Australia_and_New_Zealand_subunits.geojson";
//                 File geojsonfile = new File(getClass().getClassLoader().getResource(filename).getFile());

//                 Path path = Paths.get(geojsonfile.getAbsolutePath());
//                 String name = "geojsonfile";
//                 String originalFileName = geojsonfile.getName();
//                 String contentType = "text/plain";
//                 byte[] content = Files.readAllBytes(path);

//                 mockMvc.perform(MockMvcRequestBuilders.fileUpload("/continuous/geojson")
//                                 .file(new MockMultipartFile(name, originalFileName, contentType, content))
//                                 .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
//         }

// }

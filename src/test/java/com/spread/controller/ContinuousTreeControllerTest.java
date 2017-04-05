package com.spread.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.spread.domain.ContinuousTreeModelEntity;
import com.spread.utils.TestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ContinuousTreeControllerTest {

	private static boolean setUp = false;

	@Autowired
	private WebApplicationContext webContext;

	private MockMvc mockMvc;

	@Before
	public void setupMockMvc() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(webContext) //
				.build();

		if (setUp) {
			return;
		}

		uploadTree();
		setUp = true;
	}

	public void uploadTree() throws Exception {
		String filename = "continuous/speciesDiffusion.MCC.tre";
		File treefile = new File(getClass().getClassLoader().getResource(filename).getFile());

		Path path = Paths.get(treefile.getAbsolutePath());
		String name = "treefile";
		String originalFileName = treefile.getName();
		String contentType = "text/plain";
		byte[] content = Files.readAllBytes(path);

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/continuous/tree")
				.file(new MockMultipartFile(name, originalFileName, contentType, content))).andExpect(status().isOk());

	}

	@Test
	public void firstTest() {
		assertEquals(0, 0);
	}

	@Test
	public void attributesTest() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/continuous/attributes")).andExpect(status().isOk())
				.andExpect(content().string(TestUtils.attributes));
	}

	@Test
	public void coordinatesTest() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/continuous/coordinates/y").param("attribute", TestUtils.yCoordinate))
				.andExpect(status().isOk());
		mockMvc.perform(
				MockMvcRequestBuilders.post("/continuous/coordinates/x").param("attribute", TestUtils.xCoordinate))
				.andExpect(status().isOk());

		String content = mockMvc.perform(MockMvcRequestBuilders.get("/continuous/model")).andReturn().getResponse()
				.getContentAsString();

		String xCoordinate = new Gson().fromJson(content, ContinuousTreeModelEntity.class).getxCoordinate();
		String yCoordinate = new Gson().fromJson(content, ContinuousTreeModelEntity.class).getyCoordinate();

		assertEquals(TestUtils.xCoordinate, xCoordinate);
		assertEquals(TestUtils.yCoordinate, yCoordinate);
	}

	@Test
	public void externalAnnotationsTest() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/continuous/external-annotations")
				.param("has-external-annotations", "true")).andExpect(status().isOk());
	}

	@Test
	public void hpdLevelTest() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/continuous/hpd-level").param("hpd-level", "1.1"))
				.andExpect(status().isUnprocessableEntity());
		mockMvc.perform(MockMvcRequestBuilders.post("/continuous/hpd-level").param("hpd-level", "0.95"))
				.andExpect(status().isOk());
	}

	@Test
	public void timescaleMultiplierTest() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/continuous/timescale-multiplier").param("timescale-multiplier", "-1.0"))
				.andExpect(status().isUnprocessableEntity());
		mockMvc.perform(
				MockMvcRequestBuilders.post("/continuous/timescale-multiplier").param("timescale-multiplier", "1.0"))
				.andExpect(status().isOk());
	}

	@Test
	public void geojsonTest() throws Exception {
		String filename = "geojson/subregion_Australia_and_New_Zealand_subunits.geojson";
		File geojsonfile = new File(getClass().getClassLoader().getResource(filename).getFile());

		Path path = Paths.get(geojsonfile.getAbsolutePath());
		String name = "geojsonfile";
		String originalFileName = geojsonfile.getName();
		String contentType = "text/plain";
		byte[] content = Files.readAllBytes(path);

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/continuous/geojson")
				.file(new MockMultipartFile(name, originalFileName, contentType, content))).andExpect(status().isOk());
	}

}

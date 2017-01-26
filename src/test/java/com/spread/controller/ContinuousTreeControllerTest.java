package com.spread.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.spread.utils.TestUtils;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class ContinuousTreeControllerTest {

	private static boolean setUp = false;
	
	@Autowired
	private MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		if(setUp) {
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
				.file(new MockMultipartFile(name, originalFileName, contentType, content))).andExpect(status().is(200));
	}

	@Test
	public void attributesTest() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/continuous/attributes")).andExpect(status().isOk())
				.andExpect(content().string(TestUtils.attributes));
	}

	//TODO
	@Test
	public void coordinatesTest() throws Exception {
	
	
	
	}
	
	
}

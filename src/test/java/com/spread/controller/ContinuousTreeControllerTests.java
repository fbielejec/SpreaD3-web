package com.spread.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.spread.model.storage.StorageService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class ContinuousTreeControllerTests {

	@Autowired
	private MockMvc mockMvc;

//	@MockBean
//	private StorageService storageService;

	@Before
	public void init() {
		
	}
	
	@Test
	public void treeTest() throws Exception {
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

	// TODO
	@Test
	public void attributesTest() {

		
		
		
		
	}

}

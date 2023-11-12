package com.aws.ccproject.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.aws.ccproject.exception.ImageUploadException;
import com.aws.ccproject.service.ImgService;

@Controller
public class WebTierController {

	private static final Logger log = LoggerFactory.getLogger(WebTierController.class);

	@Autowired
	private ImgService imgService;

	private Set<String> imgNameSet = new HashSet<>();

	@PostMapping(value = "/upload")
	@ResponseBody
	public String uploadFiles(Model model, @RequestPart(value = "myfile") MultipartFile[] multipartFiles)
			throws ImageUploadException {
		imgNameSet.clear();
		String res = null;
		try {
			log.info("Images received from user, multipartFiles: ");
			for (MultipartFile multipartFile : multipartFiles) {
				log.info("Single multipartFile: " + multipartFile);
				String imgName = imgService.uploadFiles(multipartFile);
				log.info("Sending to InputQueue, imageName: " + imgName + ", multipartFile.getName(): "
						+ multipartFile.getName());
				imgNameSet.add(imgName);
				imgService.sendImageToQueue(imgName, multipartFile.getName());
				res =  getImageRecogResults1(imgName);
				log.info(res);
			}
		} catch (Exception e) {
			throw new ImageUploadException();
		}
		return res;
	}

	@GetMapping(value = "/results")
	public String getImageRecogResults(Model model) {
		List<String> resList = new ArrayList<>();
		for (String imgName : imgNameSet) {
			String imgRes = "(" + imgName.substring(0, imgName.length() - 5) + ":" + imgService.getFromSQS(imgName) + ")";
			resList.add(imgRes);
		}
		model.addAttribute("results", resList);
		return "resultsFinal";
	}
	
	public String getImageRecogResults1(String imgName) {
		String imgRes = "(" + imgName.substring(0, imgName.length() - 5) + ", " + imgService.getFromSQS(imgName) + ")";
		return imgRes;
	}

	@GetMapping(value = "/")
	String index() {
		return "index";
	}

	@GetMapping(value = "/upload")
	String uploadForm() {
		return "imageRecog";
	}
}

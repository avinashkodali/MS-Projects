package com.aws.ccproject.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ImgService {

	String uploadFiles(MultipartFile multipartFile) throws IOException;

	void sendImageToQueue(String imgUrl, String fName);

	String getFromSQS(String imgName);

}

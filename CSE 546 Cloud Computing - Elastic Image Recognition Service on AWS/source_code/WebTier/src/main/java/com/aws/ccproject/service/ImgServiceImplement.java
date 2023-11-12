package com.aws.ccproject.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;
import com.aws.ccproject.constants.Constants;

@Service
public class ImgServiceImplement implements ImgService {

	private static final Logger log = LoggerFactory.getLogger(ImgServiceImplement.class);

	private static final String FILE_NAME_DOES_NOT_EXISTS = "The image doesn't have a name attached";

	private static Hashtable<String, String> hashTable = new Hashtable<String, String>();

	@Autowired
	private SQSService sqsService;

	@Autowired
	private S3Service s3Service;

	@Async
	@Override
	public String uploadFiles(final MultipartFile multipartFile) throws IOException {
		log.info("File upload in progress..");
		String imgName = "";
		log.info("Received multipartFile: " + multipartFile);
		try {
			File f = convertMultiPartFileToFile(multipartFile);
			log.info("Converted File: " + f);
			s3Service.uploadFileToS3Bkt(Constants.INPUT_S3, f);
			log.info("File upload completed" + multipartFile.getName());
			imgName = f.getName();
		} catch (final AmazonServiceException ex) {
			log.info("File upload failed");
			log.error("Error= {} while uploading file", ex.getMessage());
		}
		return imgName;
	}

	@Override
	public void sendImageToQueue(String imgName, String fName) {
		sqsService.sendMsg(imgName, Constants.INPUT_SQS, 0);
	}

	private File convertMultiPartFileToFile(MultipartFile multipartFile) throws IOException {
		if (Objects.isNull(multipartFile.getOriginalFilename())) {
			throw new RuntimeException(FILE_NAME_DOES_NOT_EXISTS);
		}
		File f = new File(multipartFile.getOriginalFilename());
		FileOutputStream outputStream = new FileOutputStream(f);
		try {
			outputStream.write(multipartFile.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			outputStream.close();
		}
		return f;
	}

	public String getFromSQS(String imgName) {
		while (true) {
			String predName = hashTable.get(imgName);
			if (predName == null) {
				List<Message> outputMsgFromQueue = sqsService.receiveMsg(Constants.OUTPUT_SQS, 15, 15);
				log.info("outputMsgFromQueue:" + outputMsgFromQueue);
				if (outputMsgFromQueue == null) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				for (Message outputMsg : outputMsgFromQueue) {
					String outputMsgBodyFromQueue = outputMsg.getBody();
					String[] tokens = outputMsgBodyFromQueue.split(":");
					Integer count = 0;
					String imgNameInQueue = null;
					String prediction = null;
					for (String s : tokens) {
						if (count == 0)
							imgNameInQueue = s;
						else
							prediction = s;
						count++;
					}
					hashTable.put(imgNameInQueue, prediction);
				}
				sqsService.deleteMsg(outputMsgFromQueue, Constants.OUTPUT_SQS);
				predName = hashTable.get(imgName);
				if (predName != null){
					return predName;
				}
			} else {
				return predName;
			}
		}
	}
}

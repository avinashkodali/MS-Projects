package com.aws.ccproject.repo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.aws.ccproject.config.AwsConf;
import com.aws.ccproject.constants.Constants;

@Repository
public class SQSRepositoryImplement implements SQSRepository {
	
	private static Logger log = LoggerFactory.getLogger(SQSRepositoryImplement.class);

	@Autowired
	private AwsConf awsConfiguration;

	@Override
	public void delMsgBatch(List<Message> msgs, String qName) {
		log.info("Deleting msg batch in queue..");
		String qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		List<DeleteMessageBatchRequestEntry> batchEntries = new ArrayList<>();
		
		for(Message msg : msgs) {
			DeleteMessageBatchRequestEntry entry = new DeleteMessageBatchRequestEntry(msg.getMessageId(), msg.getReceiptHandle());
			batchEntries.add(entry);
		}
		DeleteMessageBatchRequest batch = new DeleteMessageBatchRequest(qUrl, batchEntries);
		awsConfiguration.awsSQS().deleteMessageBatch(batch);
	}

	@Override
	public CreateQueueResult createQueue(String qName) {
		log.info("Creating queue..");
		CreateQueueResult createQueueRes = awsConfiguration.awsSQS().createQueue(qName);
		return createQueueRes;
	}

	public String imageRecognition(String imgName) {
		log.info("Running deep learning model..");
		String s3ImgUrl = "s3://" + Constants.INPUT_S3 + "/" + imgName;
		log.info("s3ImageUrl: " + s3ImgUrl);
		GetObjectRequest req = new GetObjectRequest(Constants.INPUT_S3, imgName);
		S3Object obj = awsConfiguration.awsS3().getObject(req);
		S3ObjectInputStream objContent = obj.getObjectContent();
		log.info("s3ImageUrl: " + s3ImgUrl);
		try {
			log.info("Downloading to location: ");
			IOUtils.copy(objContent, new FileOutputStream("/home/ubuntu/app-tier/" + imgName));
		} catch (FileNotFoundException e) {
			log.info("FileNotFoundException");
			e.printStackTrace();
		} catch (IOException e) {
			log.info("IOException");
			e.printStackTrace();
		}
		String output = null;
		Process p;
		try {
			p = new ProcessBuilder("/bin/bash", "-c","cd  /home/ubuntu/app-tier/ && " + "python3 image_classification.py " + imgName).start();
			p.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			log.info("br: " + br);
			log.info("strError: " + stdError);
			output = br.readLine();
			log.info("termOutput: " + output);
			p.destroy();
		} catch (Exception e) {
			log.info("Error in processing image recognition");
			e.printStackTrace();
		}
		return output.trim();
	}

	public String parseURL(String urlInput) {
		log.info("Parsing deep learning model output");
		String imgname = null;
		String[] tokens = urlInput.split("/");
		for (String temp : tokens)
			imgname = temp;
		return imgname;
	}

	@Override
	public List<Message> receiveMsg(String qName, Integer waitTime, Integer visibilityTimeout) {
		log.info("Receiving msg from queue..");
		String qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		ReceiveMessageRequest recMsgReq = new ReceiveMessageRequest(qUrl);
		recMsgReq.setWaitTimeSeconds(waitTime);
		recMsgReq.setVisibilityTimeout(visibilityTimeout);
		recMsgReq.setMaxNumberOfMessages(1);
		ReceiveMessageResult recMsgRes = awsConfiguration.awsSQS().receiveMessage(recMsgReq);
		List<Message> msgList = recMsgRes.getMessages();
		return msgList.isEmpty()? null : msgList;
	}

	@Override
	public void sendMsg(String msgBody, String qName, Integer delaySec) {
		log.info("Sending msg to queue..");
		String qUrl = null;
		try {
			qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		} catch (QueueDoesNotExistException queueDoesNotExistException) {
			CreateQueueResult createQueueRes = this.createQueue(qName);
		}
		qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		SendMessageRequest sendMsgReq = new SendMessageRequest().withQueueUrl(qUrl).withMessageBody(msgBody).withDelaySeconds(delaySec);
		awsConfiguration.awsSQS().sendMessage(sendMsgReq);
	}

}

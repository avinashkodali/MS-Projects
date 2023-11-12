package com.aws.ccproject.service;

import com.aws.ccproject.repo.SQSRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;

@Service
public class SQSServiceImplement implements SQSService {
	
	@Autowired
	private SQSRepository sqsRepo;
	
	@Override
	public void delMsgBatch(List<Message> msgs, String qName) {
		sqsRepo.delMsgBatch(msgs, qName);
	}
	
	@Override
	public CreateQueueResult createQueue(String qName) {
		CreateQueueResult createQueueRes = sqsRepo.createQueue(qName);
		return createQueueRes;
	}
	
	@Override
	public String imageRecognitionOutput(String imgName) {
		String predRes = sqsRepo.imageRecognition(imgName);
		return predRes;
	}
	
	@Override
	public List<Message> receiveMsg(String qName, Integer waitTime, Integer visibilityTimeout) {
		return sqsRepo.receiveMsg(qName, waitTime, visibilityTimeout);
	}

	@Override
	public void sendMsg(String msgBody, String qName, Integer delaySec) {
		sqsRepo.sendMsg(msgBody, qName, delaySec);
	}
}

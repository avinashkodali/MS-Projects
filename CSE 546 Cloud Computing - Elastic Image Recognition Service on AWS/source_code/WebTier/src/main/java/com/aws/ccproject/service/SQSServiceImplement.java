package com.aws.ccproject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;
import com.aws.ccproject.repo.SQSRepository;

@Service
public class SQSServiceImplement implements SQSService {

	@Autowired
	private SQSRepository sqsRepo;

	@Override
	public void deleteMsg(List<Message> msg, String qName) {
		sqsRepo.deleteMsg(msg, qName);
	}

	@Override
	public CreateQueueResult createQueue(String qName) {
		CreateQueueResult createQueueRes = sqsRepo.createQueue(qName);
		return createQueueRes;
	}

	public List<Message> receiveMsg(String qName, Integer waitTime, Integer visibilityTimeout) {
		return sqsRepo.receiveMsg(qName, waitTime, visibilityTimeout);
	}

	@Override
	public void sendMsg(String msgBody, String qName, Integer delaySec) {
		sqsRepo.sendMsg(msgBody, qName, delaySec);
	}

	@Override
	public Integer getApproxNoMsgs(String qName) {
		return sqsRepo.getApproxNoMsgs(qName);
	}

}

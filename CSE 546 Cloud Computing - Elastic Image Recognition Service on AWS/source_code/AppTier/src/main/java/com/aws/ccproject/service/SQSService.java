package com.aws.ccproject.service;

import java.util.List;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;

public interface SQSService {
	
	public CreateQueueResult createQueue(String qName);
	
	void delMsgBatch(List<Message> msgs, String qName);
	
	public List<Message> receiveMsg(String qName, Integer waitTime, Integer visibilityTimeout);
	
	public void sendMsg(String msgBody, String qName, Integer delaySec);

	public String imageRecognitionOutput(String imgName);
	
}

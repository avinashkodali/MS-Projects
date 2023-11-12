package com.aws.ccproject.repo;

import java.util.List;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;

public interface SQSRepository {
	
	public CreateQueueResult createQueue(String qName);
	
	public void sendMsg(String msgBody, String qName, Integer delaySec);
	
	public List<Message> receiveMsg(String qName, Integer waitTime, Integer visibilityTimeout);

	public String imageRecognition(String imgName);

	void delMsgBatch(List<Message> msgs, String qName);

}

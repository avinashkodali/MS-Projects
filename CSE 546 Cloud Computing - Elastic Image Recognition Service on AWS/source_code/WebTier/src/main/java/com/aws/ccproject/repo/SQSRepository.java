package com.aws.ccproject.repo;

import java.util.List;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;

public interface SQSRepository {

	CreateQueueResult createQueue(String qName);

	List<Message> receiveMsg(String qName, Integer waitTime, Integer visibilityTimeout);

	void sendMsg(String msgBody, String qName, Integer delaySec);
	
	Integer getApproxNoMsgs(String qName);

	void deleteMsg(List<Message> msg, String qName);

}

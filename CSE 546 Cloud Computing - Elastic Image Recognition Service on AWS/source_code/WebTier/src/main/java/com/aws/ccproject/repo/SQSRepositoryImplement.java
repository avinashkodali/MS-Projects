package com.aws.ccproject.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.aws.ccproject.config.AwsConf;

@Repository
public class SQSRepositoryImplement implements SQSRepository {

	private static final Logger log = LoggerFactory.getLogger(SQSRepositoryImplement.class);

	@Autowired
	private AwsConf awsConfiguration;

	@Override
	public void deleteMsg(List<Message> msgs, String qName) {
		log.info("Deleting msg batch from queue..");
		String qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		List<DeleteMessageBatchRequestEntry> batchEntries = new ArrayList<>();
		for (Message msg : msgs) {
			DeleteMessageBatchRequestEntry entry = new DeleteMessageBatchRequestEntry(msg.getMessageId(),msg.getReceiptHandle());
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

	@Override
	public List<Message> receiveMsg(String qName, Integer waitTime, Integer visibilityTimeout) {
		log.info("Receiving msg batch from queue..");
		String qUrl = null;
		try {
			qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		} catch (QueueDoesNotExistException queueDoesNotExistException) {
			CreateQueueResult createQueueRes = this.createQueue(qName);
			qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		}
		ReceiveMessageRequest recMsgReq = new ReceiveMessageRequest(qUrl);
		recMsgReq.setMaxNumberOfMessages(10);
		recMsgReq.setWaitTimeSeconds(waitTime);
		recMsgReq.setVisibilityTimeout(visibilityTimeout);
		ReceiveMessageResult recMsgRes = awsConfiguration.awsSQS().receiveMessage(recMsgReq);
		List<Message> msgList = recMsgRes.getMessages();
		return msgList.isEmpty() ? null : msgList;
	}

	@Override
	public void sendMsg(String msgBody, String qName, Integer delaySec) {
		log.info("Sending msg into queue:" + msgBody);
		String qUrl = null;
		try {
			qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		} catch (QueueDoesNotExistException queueDoesNotExistException) {
			log.info("SQS queue not in list creating now: " + qName);
			CreateQueueResult createQueueRes = this.createQueue(qName);
			qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		}
		SendMessageRequest sendMsgReq = new SendMessageRequest().withQueueUrl(qUrl).withMessageBody(msgBody).withDelaySeconds(delaySec);
		awsConfiguration.awsSQS().sendMessage(sendMsgReq);

	}

	@Override
	public Integer getApproxNoMsgs(String qName) {
		log.info("Getting appox no. of msgs..");
		String qUrl = null;
		try {
			qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		} catch (QueueDoesNotExistException queueDoesNotExistException) {
			CreateQueueResult createQueueRes = this.createQueue(qName);
			qUrl = awsConfiguration.awsSQS().getQueueUrl(qName).getQueueUrl();
		}
		List<String> attribs = new ArrayList<String>();
		attribs.add("ApproximateNumberOfMessages");
		GetQueueAttributesRequest getQueueAttribReq = new GetQueueAttributesRequest(qUrl, attribs);
		Map<String, String> map = awsConfiguration.awsSQS().getQueueAttributes(getQueueAttribReq).getAttributes();
		String nMsgsStr = (String) map.get("ApproximateNumberOfMessages");
		Integer nMsgsInt = Integer.valueOf(nMsgsStr);
		return nMsgsInt;
	}
}

package com.aws.ccproject.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.model.Message;
import com.aws.ccproject.constants.Constants;

@Service
public class ListenDispatchServiceImplement implements ListenDispatchService {
	
	private static Logger log = LoggerFactory.getLogger(ListenDispatchServiceImplement.class);

	@Autowired
	private SQSService sqsService;

	@Autowired
	private S3Service s3Service;

	@Autowired
	private EC2Service ec2Service;

	@Override
	public void generalMethod() {
		while (true) {
			List<Message> inputMsgs = sqsService.receiveMsg(Constants.INPUT_SQS, 0, 180);
			if (inputMsgs == null)
				break;
			for(Message msg : inputMsgs) {
				String imgName = msg.getBody();
				log.info("Msg: " + msg + ", img name: " + imgName);
				String predRes = sqsService.imageRecognitionOutput(imgName);
				if (predRes == null) {
					predRes = Constants.NO_PREDICTION;
				}
				log.info(Constants.PREDICTION + predRes);
				s3Service.putObject(imgName.substring(0, imgName.length() - 5), predRes);
				sqsService.sendMsg(imgName + ":" + predRes, Constants.OUTPUT_SQS, 0);
			}
			sqsService.delMsgBatch(inputMsgs, Constants.INPUT_SQS);
		}
		ec2Service.endInstance();
	}
}

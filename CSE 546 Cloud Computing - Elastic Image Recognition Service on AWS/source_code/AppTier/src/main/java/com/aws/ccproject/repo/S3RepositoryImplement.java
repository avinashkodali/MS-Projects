package com.aws.ccproject.repo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.aws.ccproject.config.AwsConf;
import com.aws.ccproject.constants.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class S3RepositoryImplement implements S3Repository {
	
	private static Logger log = LoggerFactory.getLogger(S3RepositoryImplement.class);
	
	@Autowired
	private AwsConf awsConfiguration;

	@Override
	public Bucket createBkt() {
		Bucket s3bkt = null;
		if (awsConfiguration.awsS3().doesBucketExistV2(Constants.OUTPUT_S3)) {
			log.info(Constants.GET_BUCKET + Constants.OUTPUT_S3);
			s3bkt = getBkt();
		} else {
			log.info(Constants.CREATE_BUCKET + Constants.OUTPUT_S3);
			s3bkt = awsConfiguration.awsS3().createBucket(Constants.OUTPUT_S3);
		}
		return s3bkt;
	}

	@Override
	public Bucket getBkt() {
		Bucket s3bkt = null;
		List<Bucket> bkts = awsConfiguration.awsS3().listBuckets();
		for (Bucket bkt : bkts) {
			if (bkt.getName().equals(Constants.OUTPUT_S3))
				s3bkt = bkt;
		}
		return s3bkt;
	}

	@Override
	public void putObject(String key, String value) {
		log.info(Constants.INSERT_INTO_BUCKET);
		this.createBkt();
		@SuppressWarnings("serial")
		Map<String, String> predMap = new HashMap<String, String>() {{
				put(key, value);
		}};
		try {
			String pred = new ObjectMapper().writeValueAsString(predMap);
			log.info("Saving output for image: " + key);
			ObjectMetadata meta = new ObjectMetadata();
			meta.setContentLength(pred.length());
			InputStream stream = new ByteArrayInputStream(pred.getBytes(StandardCharsets.UTF_8));
			final PutObjectRequest putObjReq = new PutObjectRequest(Constants.OUTPUT_S3, pred, stream, meta);
			awsConfiguration.awsS3().putObject(putObjReq);

		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}

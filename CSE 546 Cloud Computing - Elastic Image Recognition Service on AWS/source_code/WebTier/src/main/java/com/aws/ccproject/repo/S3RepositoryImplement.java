package com.aws.ccproject.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.aws.ccproject.config.AwsConf;
import com.aws.ccproject.constants.Constants;

@Repository
public class S3RepositoryImplement implements S3Repository {

	private static final Logger log = LoggerFactory.getLogger(S3RepositoryImplement.class);

	@Autowired
	private AwsConf awsConfiguration;

	@Override
	public void uploadFileToS3Bkt(final String bktName, final File file) {
		createBkt(bktName);
		final String fName = file.getName();
		log.info("Uploading file:" + fName);
		final PutObjectRequest putObjReq = new PutObjectRequest(bktName, fName, file);
		awsConfiguration.awsS3().putObject(putObjReq);
	}

	@Override
	public Bucket createBkt(String bktName) {
		Bucket s3bkt = null;
		if (awsConfiguration.awsS3().doesBucketExistV2(bktName)) {
			log.info(Constants.GET_BUCKET + bktName);
			s3bkt = getBkt(bktName);
		} else {
			log.info(Constants.CREATE_BUCKET + bktName);
			s3bkt = awsConfiguration.awsS3().createBucket(bktName);
		}
		return s3bkt;
	}

	@Override
	public Bucket getBkt(String bktName) {
		Bucket s3bkt = null;
		List<Bucket> bkts = awsConfiguration.awsS3().listBuckets();
		for (Bucket bkt : bkts) {
			if (bkt.getName().equals(bktName))
				s3bkt = bkt;
		}
		return s3bkt;
	}

	@Override
	public List<String> getResponseResults() {
		ListObjectsRequest listObjReq = new ListObjectsRequest().withBucketName(Constants.OUTPUT_S3);

		List<String> keys = new ArrayList<>();

		ObjectListing objs = awsConfiguration.awsS3().listObjects(listObjReq);

		for (S3ObjectSummary item : objs.getObjectSummaries()) {
			keys.add(item.getKey());
		}
		return keys;
	}
}

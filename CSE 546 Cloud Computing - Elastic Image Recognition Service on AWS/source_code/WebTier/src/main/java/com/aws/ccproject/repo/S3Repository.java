package com.aws.ccproject.repo;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.s3.model.Bucket;

@Repository
public interface S3Repository {

	List<String> getResponseResults();

	Bucket getBkt(String bktName);

	Bucket createBkt(String bktName);

	void uploadFileToS3Bkt(String bktName, File file);
}

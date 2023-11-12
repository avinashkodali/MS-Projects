package com.aws.ccproject.service;

import java.io.File;
import java.util.List;

public interface S3Service {
	
	List<String> getResponseResults();

	void uploadFileToS3Bkt(final String bktName, final File file);

}

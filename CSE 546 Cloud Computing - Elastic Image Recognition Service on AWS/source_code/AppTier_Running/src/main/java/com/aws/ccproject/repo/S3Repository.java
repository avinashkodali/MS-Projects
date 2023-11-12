package com.aws.ccproject.repo;

import com.amazonaws.services.s3.model.Bucket;

public interface S3Repository {
	
	public Bucket createBkt();
	
	public Bucket getBkt();
	
	public void putObject(String key, String value);

}

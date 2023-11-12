package com.aws.ccproject.service;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aws.ccproject.repo.S3Repository;

@Service
public class S3ServiceImplement implements S3Service {
	
	@Autowired
	private S3Repository s3Repo;
	
	@Override
    public List<String> getResponseResults() {
		return s3Repo.getResponseResults();
	}
	
	@Override
	public void uploadFileToS3Bkt(String bktName, File file){
		s3Repo.uploadFileToS3Bkt(bktName, file);
	}
}
package com.aws.ccproject.constants;

import com.amazonaws.regions.Regions;

public class Constants {

	public static final String AWS_ACCESS_KEY = "xxxxx";

	public static final String AWS_SECRET_KEY = "xxxxx";

	public static final Regions AWS_REGION = Regions.US_EAST_1;

	public static final String INPUT_SQS = "xxxxx";

	public static final String OUTPUT_SQS = "xxxx";

	public static final String INPUT_S3 = "xxxxxx";

	public static final String OUTPUT_S3 = "xxxxxx";

	public static final String AWS_SECURITY_GROUP_ID1 = "xxxxx";
	
	public static final String AWS_SECURITY_GROUP_ID2 = "xxxxxxx";
	
	public static final Integer MAXIMUM_RUNNING_INSTANCES = 19;
	
	public static final Integer  MAX_REQUESTS_PER_INSTANCE = 5;

	public static final Integer EXISTING_INSTANCES = 1;

	public static final String IMAGE_ID = "ami-xxxxxx";

	public static final String INSERT_INTO_BUCKET = "Inserting Object Into S3 Bucket...";

	public static final String CREATE_BUCKET = "Creating S3 Bucket...";

	public static final String GET_BUCKET = "Getting existing S3 Bucket...";

}

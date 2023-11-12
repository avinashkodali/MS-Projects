package com.aws.ccproject.constants;

import com.amazonaws.regions.Regions;

public class Constants {

	public static final String AWS_ACCESS_KEY = "xxxxxxxx";

	public static final String AWS_SECRET_KEY = "xxxxxxxx";

	public static final Regions AWS_REGION = Regions.US_EAST_1;
	
	public static final String INPUT_S3 = "xxxxxxx";

	public static final String OUTPUT_S3 = "xxxxxx";

	public static final String INPUT_SQS = "xxxxxx";

	public static final String OUTPUT_SQS = "xxxxxx";
	
	
	
	//Log messages for application
	
	public static final String INSERT_INTO_BUCKET = "Inserting Object Into S3 Bucket...";
	
	public static final String CREATE_BUCKET = "Creating S3 Bucket...";
	
	public static final String GET_BUCKET = "Getting existing S3 Bucket...";
	
	
	public static final String NO_PREDICTION ="No Prediction Value";
	
	public static final String PREDICTION ="Image Predicted Value:";
	
	public static final String END_INSTANCE="Ending the current instance: ";
	

}

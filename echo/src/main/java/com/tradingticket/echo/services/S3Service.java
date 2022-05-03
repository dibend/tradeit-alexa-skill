package com.tradingticket.echo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.auth.BasicAWSCredentials;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

public class S3Service {
    private BasicAWSCredentials awsCreds;
    private AmazonS3 s3client;
    private UtilsService utilsService = UtilsService.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(S3Service.class);

    private static S3Service instance = null;
    protected S3Service() {
	String accessKeyId = utilsService.getProp("s3.credentials.accesskeyid");
	String secretAccessKey = utilsService.getProp("s3.credentials.secretaccesskey");
	awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);
	s3client = new AmazonS3Client(awsCreds);
    }

    public static S3Service getInstance() {
        if(instance == null) {
            instance = new S3Service();
        }
        return instance;
    }

    public String getObjectData(String bucketName, String objectName) {
	String objectString = null;
	try {
                S3Object object = s3client.getObject(new GetObjectRequest(bucketName, objectName));
                InputStream objectData = object.getObjectContent();
                // Process the objectData stream.
                objectString = utilsService.convertStreamToString(objectData);
                try {
		    objectData.close();
		}
                catch(IOException ex) {
		    LOG.error("error closing S3 stream", ex);
		}
        }
        catch (AmazonServiceException ase) {}
	
	return objectString;
    }

    public int putObjectData(String bucketName, String objectName, String objectString) {
        InputStream stream;
        try {
	    stream = new ByteArrayInputStream(objectString.getBytes("UTF-8"));
	}
        catch(IOException ex) {
	    LOG.error("error writing to S3", ex);
	    return 1;
	}

        ObjectMetadata md = new ObjectMetadata();
        md.setContentLength((long)objectString.length());

        try {
            s3client.putObject(new PutObjectRequest(bucketName, objectName, stream, md));
        } catch (AmazonServiceException ase) {
	    LOG.error("error writing to S3", ase);
            return 2;
        } catch (AmazonClientException ace) {
	    LOG.error("error writing to S3", ace);
            return 3;
        }

	return 0;
    }
}

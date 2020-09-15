package org.unicon.lex;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.AmazonAthenaClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.util.Properties;

public class AwsFactory {
    private static final int CLIENT_EXECUTION_TIMEOUT = 60000;
    private Properties properties;

    public AwsFactory(Properties properties) {
        this.properties = properties;
    }

    public AmazonAthena getAthenaClient() {
        String region = (String) properties.getOrDefault("aws_region", "us-east-1");
        DefaultAWSCredentialsProviderChain provider = new DefaultAWSCredentialsProviderChain();
        AmazonAthenaClientBuilder builder = AmazonAthenaClientBuilder.standard()
              .withRegion(region)
              .withCredentials(provider)
              .withClientConfiguration(new ClientConfiguration().withClientExecutionTimeout(CLIENT_EXECUTION_TIMEOUT));
        return builder.build();
    }

    public AmazonS3 getS3Client() {
        String region = (String) properties.getOrDefault("aws_region", "us-east-1");
        DefaultAWSCredentialsProviderChain provider = new DefaultAWSCredentialsProviderChain();
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
              .withRegion(region)
              .withCredentials(provider)
              .withClientConfiguration(new ClientConfiguration().withClientExecutionTimeout(CLIENT_EXECUTION_TIMEOUT));
        return builder.build();
    }
}

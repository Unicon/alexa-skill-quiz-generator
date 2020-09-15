package org.unicon.lex.services.external;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.Record;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KinesisService {
    private final Logger log = LogManager.getLogger(getClass());

    private static final String KINESIS_DELIVERYSTREAM_KEY = "kinesis.deliverystream";
    private final AmazonKinesisFirehose firehoseClient;
    private final ObjectMapper objectMapper;
    private final String deliveryStreamName;

    public KinesisService(Properties properties) {
        String deliveryStreamName = properties.getProperty(KINESIS_DELIVERYSTREAM_KEY);
        log.error("creating kinesisService");
        firehoseClient = AmazonKinesisFirehoseClientBuilder.defaultClient();
        objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(df);
        this.deliveryStreamName = deliveryStreamName;
    }

    public void publish(KinesisEvent event) {
        log.error("in Kinesis, publishing");
        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setDeliveryStreamName(deliveryStreamName);

        String data = convertToString(event);
        log.error("data to publish to firehose:[" + data + "]");
        Record record = new Record().withData(ByteBuffer.wrap(data.getBytes()));

        putRecordRequest.setRecord(record);
        firehoseClient.putRecord(putRecordRequest);
    }

    public String convertToString(KinesisEvent event) {
        String data = null;
        try {
            data = objectMapper.writeValueAsString(event) + "\n";
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error(e.getMessage(),  e);
        }
        log.error("data {}",  data);
        return data;
    }
}

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.gson.Gson;

import javax.jms.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws JMSException, InterruptedException {

        SQSConnectionFactory connectionFactory=new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_2)
        );

        SQSConnection connection=connectionFactory.createConnection();

        Session session=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);

        Queue queue=session.createQueue("vbedi-sqs-queue");

        // ******* Send messages ********
        /*MessageProducer producer=session.createProducer(queue);

        TextMessage message=session.createTextMessage("Test Message");

        producer.send(message);
        System.out.println("JMS message "+message.getJMSMessageID());*/


        // ********* Receive messages synchronously ********

       /* MessageConsumer consumer=session.createConsumer(queue);
        connection.start();

        // Wait for 1 second
        Message receivedMessage=consumer.receive(1000);

        if(receivedMessage!=null){
            System.out.println(((TextMessage)receivedMessage).getText());
        }

        connection.close();*/

        // ******* Receive message async *********
        MessageConsumer consumer=session.createConsumer(queue);
        consumer.setMessageListener(new MyListener());
        connection.start();
        Thread.sleep(1000);


    }
}

class RecordContainer {
    ArrayList<Record> Records;
}

class Record{
    public String awsRegion;
    public S3Response s3;
}

class S3Response{
    public S3ObjectResponse object;
    public S3BucketResponse bucket;
}

class S3BucketResponse{
    public String name;
}

class S3ObjectResponse{
    public String key;
}
class MyListener implements MessageListener{
    @Override
    public void onMessage(Message message) {
        try{
            String text = ((TextMessage) message).getText();
            RecordContainer sqsResponse=getSQSResponse(text);
            System.out.println("AWS REGION "+sqsResponse.Records.get(0).awsRegion);
            System.out.println("AWS BUCKET NAME "+sqsResponse.Records.get(0).s3.bucket.name);
            System.out.println("AWS FILE NAME "+sqsResponse.Records.get(0).s3.object.key);

        }catch(JMSException e){
            e.printStackTrace();
        }
    }

    public RecordContainer getSQSResponse(String s){
        Gson gson=new Gson();

        return gson.fromJson(s.replaceAll("\\s+", ""),RecordContainer.class);

    }
}



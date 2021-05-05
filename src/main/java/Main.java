import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import javax.jms.*;

public class Main {
    public static void main(String[] args) throws JMSException {

        SQSConnectionFactory connectionFactory=new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_2)
        );

        SQSConnection connection=connectionFactory.createConnection();

        Session session=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);

        Queue queue=session.createQueue("vbedi-sqs-queue");

        MessageProducer producer=session.createProducer(queue);

        TextMessage message=session.createTextMessage("Test Message");

        producer.send(message);
        System.out.println("JMS message "+message.getJMSMessageID());
    }
}

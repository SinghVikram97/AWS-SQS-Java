import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import javax.jms.*;

public class Main1 {
    public static void main(String[] args) throws JMSException, InterruptedException {
        SQSConnectionFactory connectionFactory=new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_2)
        );

        SQSConnection connection=connectionFactory.createConnection();

        Session session=connection.createSession(false,Session.CLIENT_ACKNOWLEDGE);

        Queue queue=session.createQueue("vbedi-sqs-queue");

        MessageConsumer consumer=session.createConsumer(queue);
        consumer.setMessageListener(new MyListener1());
        connection.start();

        // Can remove Thread.sleep
    }
}
class MyListener1 implements MessageListener {
    @Override
    public void onMessage(Message message) {
        try{
            String text = ((TextMessage) message).getText();
            System.out.println(text);
            message.acknowledge();

        }catch(JMSException e){
            System.out.println("Error processing message "+e.getMessage());
            e.printStackTrace();
        }
    }
}
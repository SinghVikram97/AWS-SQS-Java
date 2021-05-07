import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import javax.jms.*;

public class Main3 {
    public static void main(String[] args) throws JMSException {

        SQSListener sqsListener=new SQSListener("vbedi-sqs-queue",Regions.US_WEST_2);
        sqsListener.makeConnection().start();

    }
};

class SQSListener{

    private String queueName;
    private Regions region;

    public SQSListener(String queueName, Regions region) {
        this.queueName = queueName;
        this.region = region;
    }

    private SQSConnectionFactory sqsConnectionFactory(){
        return new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard().withRegion(this.region)
        );
    }

    private SQSConnection connection() throws JMSException {
        return sqsConnectionFactory().createConnection();
    }

    public SQSConnection makeConnection() throws JMSException {
        SQSConnection sqsConnection=connection();
        Session session=sqsConnection.createSession(false,Session.CLIENT_ACKNOWLEDGE);
        Queue queue=session.createQueue(this.queueName);
        MessageConsumer consumer=session.createConsumer(queue);
        consumer.setMessageListener(new MyListener2());
        return sqsConnection;
    }

}


class MyListener2 implements MessageListener {
    @Override
    public void onMessage(Message message) {
        try{
            String text = ((TextMessage) message).getText();
            if(text.contains("s3:TestEvent")){
                System.out.println("Ignoring test event");

            }
            else{
                System.out.println(text);

            }
            message.acknowledge();

        }catch(JMSException e){
            e.printStackTrace();
        }
    }
}
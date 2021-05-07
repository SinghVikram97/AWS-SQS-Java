import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import javax.jms.JMSException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;

public class Main4 {
    public static void main(String[] args) throws JMSException {
        S3Handler s3Handler=new S3Handler(Regions.US_WEST_2);
        Bucket b=s3Handler.createBucket("vbedi-test-demo-ingestion-data-bucket-1");
        s3Handler.createFolder(b.getName(),"input");
        s3Handler.enableNotification(b);

        SQSListener sqsListener=new SQSListener("vbedi-sqs-queue-1",Regions.US_WEST_2);
        sqsListener.makeConnection().start();

        // Need to ignore test event
        // And empty folder creation event
    }
}
class S3Handler {
    private final AmazonS3 s3Client;
    public S3Handler(Regions region) {
        s3Client= AmazonS3ClientBuilder.standard().withRegion(region).build();
    }

    public Bucket getBucket(String bucketName){
        Bucket b=null;
        List<Bucket> bucketList=s3Client.listBuckets();
        for(Bucket bucket:bucketList){
            if(bucket.getName().equals(bucketName)){
                b=bucket;
            }
        }
        return b;
    }

    public Bucket createBucket(String bucketName){
        Bucket b=null;
        if(s3Client.doesBucketExist(bucketName)){
            System.out.println("Bucket already exists "+bucketName);
            b=getBucket(bucketName);
        }
        else{
            try{
                b=s3Client.createBucket(bucketName);
                System.out.println("Created bucket "+bucketName);
            }catch (AmazonS3Exception e){
                System.out.println("Error creating bucket "+e.getMessage());
                e.printStackTrace();
            }
        }
        return b;
    }

    public File downloadObject(String bucketName, String key, String localFileName){
        File localFile=new File(localFileName);
        s3Client.getObject(new GetObjectRequest(bucketName,key),localFile);
        return localFile;
    }

    public void uploadObject(String bucketName,String key,File fileToUpload){

        // Check if file exists and is not empty
        // no invalid/valid records
        if(fileToUpload.exists() && fileToUpload.length()!=0){
            s3Client.putObject(
                    bucketName,
                    key,
                    fileToUpload
            );
        }
    }

    public void createFolder(String bucketName,String folderName){
        ObjectMetadata objectMetadata=new ObjectMetadata();
        objectMetadata.setContentLength(0);

        InputStream emptyContent=new ByteArrayInputStream(new byte[0]);

        PutObjectRequest putObjectRequest=new PutObjectRequest(bucketName,folderName+"/",emptyContent,objectMetadata);

        s3Client.putObject(putObjectRequest);
    }

    public void enableNotification(Bucket b){
       try {
           BucketNotificationConfiguration notificationConfiguration = new BucketNotificationConfiguration();



           // Add an SQS queue notification
           QueueConfiguration queueConfiguration = new QueueConfiguration("arn:aws:sqs:us-west-2:360877574123:vbedi-sqs-queue-1",EnumSet.of(S3Event.ObjectCreated));
           notificationConfiguration.addConfiguration("sqsQueueConfig", queueConfiguration);

           S3KeyFilter s3KeyFilter=new S3KeyFilter();
           s3KeyFilter.addFilterRule(S3KeyFilter.FilterRuleName.Prefix.newRule("input/"));

           Filter filter=new Filter();
           filter.setS3KeyFilter(s3KeyFilter);

           queueConfiguration.setFilter(filter);

           SetBucketNotificationConfigurationRequest request = new SetBucketNotificationConfigurationRequest(
                   b.getName(),
                   notificationConfiguration
           );

           s3Client.setBucketNotificationConfiguration(request);
       } catch(AmazonServiceException e){
           e.printStackTrace();
       }catch(SdkClientException e){
           e.printStackTrace();
       }
    }

}
package quark;

import java.util.Arrays;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;

import org.junit.Test;

public class FlowTest {
  @Test
  public void testFlow() {
    //Create Publisher  
    SubmissionPublisher<String> publisher = new SubmissionPublisher<>();  
  
    //Register Subscriber  
    MySubscriber<String> subscriber = new MySubscriber<>();  
    MySubscriber<String> subscriber2 = new MySubscriber<>();  
    publisher.subscribe(subscriber);  
    publisher.subscribe(subscriber2);  
    
    //Publish items  
    System.out.println("Publishing Items...");  
    String[] items = {"1", "2", "3", "4", "5", "6"};  
    Arrays.asList(items).stream().forEach(i -> publisher.submit(i));  
    publisher.close();  
  }
}
class MySubscriber<T> implements Subscriber<T> {  
  private Subscription subscription;  
  
  @Override  
  public void onSubscribe(Subscription subscription) {  
    this.subscription = subscription;  
    subscription.request(1); //a value of  Long.MAX_VALUE may be considered as effectively unbounded  
  }  
  
  @Override  
  public void onNext(T item) {  
    System.out.println("Got : " + item);  
    subscription.request(1); //a value of  Long.MAX_VALUE may be considered as effectively unbounded  
  }  
  
  @Override  
  public void onError(Throwable t) {  
    t.printStackTrace();  
  }  
  
  @Override  
  public void onComplete() {  
    System.out.println("Done");  
  }  
}  

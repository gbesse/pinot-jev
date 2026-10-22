package org.apache.pinot.function.jev;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class JevEndpointTest {
 @Test void rejectsEmbeddedCredentialsFragmentsAndInvalidPortsBeforeTransport(){
  for(String url:new String[]{"https://embedded-secret@example.com/x","https://example.com/x#private","https://example.com:99999/x","https://example.com:0/x","https:/missing-host","not a URL"}){
   IllegalArgumentException error=assertThrows(IllegalArgumentException.class,()->new JevClient("synthetic",url,"jev-1.13.0",1000,10));
   assertFalse(error.getMessage().contains("embedded-secret"));assertFalse(error.getMessage().contains("private"));
  }
 }
 @Test void rejectsCarriageReturnKeyBeforeHttpClient(){assertThrows(IllegalArgumentException.class,()->new JevClient("synthetic\rvalue","https://example.com/x","jev-1.13.0",1000,10));}
}

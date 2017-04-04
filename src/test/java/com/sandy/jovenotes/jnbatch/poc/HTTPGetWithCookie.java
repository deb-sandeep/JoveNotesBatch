package com.sandy.jovenotes.jnbatch.poc;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.client.CookieStore ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.client.methods.HttpRequestBase ;
import org.apache.http.cookie.ClientCookie ;
import org.apache.http.impl.client.BasicCookieStore ;
import org.apache.http.impl.client.HttpClientBuilder ;
import org.apache.http.impl.cookie.BasicClientCookie ;
import org.apache.http.util.EntityUtils ;

public class HTTPGetWithCookie {
    
    public void test() throws Exception {
        
        BasicClientCookie cookie = new BasicClientCookie( "auth_token", "BATCH_ROBOT_AUTH_TOKEN" ) ;
        cookie.setPath( "/" ) ;
        cookie.setAttribute( ClientCookie.DOMAIN_ATTR, "true") ;
        cookie.setDomain( "localhost" ) ;
        
        CookieStore cookieStore = new BasicCookieStore() ;
        cookieStore.addCookie( cookie );
        
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpRequestBase request = new HttpGet( "http://localhost/jove_notes/api/RoboTest" ) ;

        HttpResponse response = client.execute( request ) ;
        HttpEntity entity = response.getEntity() ;
        if( entity != null ) {
            System.out.println( EntityUtils.toString( entity ) ) ;
        }
    }
    
    public static void main( String[] args ) throws Exception {
        new HTTPGetWithCookie().test() ;
    }
}
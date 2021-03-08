package se.sigma.sallinggroup.managers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import se.sigma.sallinggroup.mongodbmodel.entity.ClassificationDocument;
import se.sigma.sallinggroup.mongodbmodel.entity.GoldenRecordDocument;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class RestDbManager implements DbManager {
    private String _url;
    private String _apiKey;
    static Logger log = Logger.getLogger(RestDbManager.class.getName());


    public RestDbManager(String url, String apiKey) {
        _url = url;
        _apiKey = apiKey;
    }


    @Override
    public void connect() throws Exception {

    }

    @Override
    public void store(GoldenRecordDocument doc) throws Exception {

        log.debug("store: Product Storing document using REST interface");
        ObjectMapper mapper = new ObjectMapper();

        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String json = mapper.writeValueAsString(doc);

        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost request = new HttpPost(_url + "/store?abc=" + _apiKey + "&type=product");
        request.setHeader("Content-type","application/json");

        HttpEntity entity = new BasicHttpEntity();
        InputStream targetStream = new ByteArrayInputStream(json.getBytes("UTF-8"));

        ((BasicHttpEntity) entity).setContent(targetStream);
        request.setEntity(entity);

        // add request header
        request.addHeader("User-Agent", "mongodbtest");
        HttpResponse response = client.execute(request);

        if( response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 299) {
            log.info("store: Product Response code " + response.getStatusLine().getStatusCode());
        } else {
            log.error("store: Product Response code " + response.getStatusLine().getStatusCode());
        }

    }

    @Override
    public void store(ClassificationDocument doc) throws Exception {
        log.debug("store: Classification Storing document using REST interface");


        ObjectMapper mapper = new ObjectMapper();

        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String json = mapper.writeValueAsString(doc);

        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost request = new HttpPost(_url + "/store?abc=" + _apiKey + "&type=classification");
        request.setHeader("Content-type","application/json");

        HttpEntity entity = new BasicHttpEntity();
        InputStream targetStream = new ByteArrayInputStream(json.getBytes("UTF-8"));

        ((BasicHttpEntity) entity).setContent(targetStream);
        request.setEntity(entity);

        // add request header
        request.addHeader("User-Agent", "mongodbtest");
        HttpResponse response = client.execute(request);

        if( response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 299) {
            log.info("store: Product Response code " + response.getStatusLine().getStatusCode());
        } else {
            log.error("store: Product Response code " + response.getStatusLine().getStatusCode());
        }

    }

    @Override
    public void delete(ClassificationDocument doc) throws Exception {

    }

    @Override
    public void delete(GoldenRecordDocument doc) throws Exception {

    }
}

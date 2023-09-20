package vn.teca.digitization.file.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.teca.digitization.file.exception.EntityValidationException;
import vn.teca.digitization.file.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HitachiFileService {
    @Autowired
    private HttpClient httpClient;

    private static final String HCP_AUTH_HEADER_KEY = "Authorization";

    @Value("${hitachi.host}")
    private String host;

    @Value("${hitachi.username}")
    private String username;

    @Value("${hitachi.password}")
    private String password;

    private String getAuth() {
        return "HCP " + Constants.getBase64Value(username) + ":" + Constants.getMD5Value(password);
    }

    public HttpEntity getObject(String path) throws IOException {
        String auth = this.getAuth();

        HttpGet request = new HttpGet(host + path);

        // add authorization header for user(base64) with password(md5)
        request.addHeader(HCP_AUTH_HEADER_KEY, auth);
        // uncomment the following to do a partial content transfer
        // request.addHeader("Range", "bytes=0-0");

        // execute the request
        HttpResponse response = httpClient.execute(request);
        return response.getEntity();
    }

    public boolean checkDirectory(String path) throws IOException {
        String auth = this.getAuth();

        HttpHead request = new HttpHead(host + path);

        // add authorization header for user(base64) with password(md5)
        request.addHeader(HCP_AUTH_HEADER_KEY, auth);

        //execute the request
        HttpResponse response = httpClient.execute(request);
        return response.getStatusLine().getStatusCode() == HttpStatus.OK.value();
    }

    public void createDirectory(String path) throws IOException, EntityValidationException {
        String auth = this.getAuth();

        HttpPut request = new HttpPut(host + path + "?type=directory");

        // add authorization header for user(base64) with password(md5)
        request.addHeader(HCP_AUTH_HEADER_KEY, auth);

        //execute the request
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()
                && response.getStatusLine().getStatusCode() != HttpStatus.CREATED.value()) {
            throw new EntityValidationException("Error create directory");
        }
    }

    public void uploadFile(String path, String localFilePath, boolean chunk) throws IOException, EntityValidationException {
        String auth = this.getAuth();
        HttpPut request = new HttpPut(host + path);
        // add authorization header for user(base64) with password(md5)
        request.addHeader(HCP_AUTH_HEADER_KEY, auth);
        File input = new File(localFilePath);
        byte[] fileAsByteArr = Constants.fileToByteArray(input);

        ByteArrayEntity requestEntity = new ByteArrayEntity(fileAsByteArr);
        if (chunk) {
            requestEntity.setChunked(true);
        }
        //set the request to use the byte array
        request.setEntity(requestEntity);
        //execute PUT request
        HttpResponse response = httpClient.execute(request);

        if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value() && response.getStatusLine().getStatusCode() != HttpStatus.CREATED.value()) {
            throw new EntityValidationException("Error upload file");
        }

    }

    public void uploadFiles(String path, byte[] fileAsByteArr) throws IOException, EntityValidationException {
        String auth = this.getAuth();
        HttpResponse response =null;
            HttpPut request = new HttpPut(host + path);
            request.addHeader(HCP_AUTH_HEADER_KEY, auth);
            ByteArrayEntity requestEntity = new ByteArrayEntity(fileAsByteArr);
            request.setEntity(requestEntity);
            //execute PUT request
            response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value() && response.getStatusLine().getStatusCode() != HttpStatus.CREATED.value()) {
            throw new EntityValidationException("Error upload file");
        }
     }

    public void updateHoldFile(String path) throws IOException{
        String auth = this.getAuth();

        HttpPost request = new HttpPost(host + path);

        // add authorization header for user(base64) with password(md5)
        request.addHeader(HCP_AUTH_HEADER_KEY, auth);

        // set field hold to true
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("hold", "true"));
        request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // execute request
        HttpResponse response = httpClient.execute(request);

        //print response status to console
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
    }

    public void deleteObject(String path) throws IOException{
        String auth = this.getAuth();

        HttpDelete request = new HttpDelete(host + path);


        // add authorization header for user(base64) with password(md5)
        request.addHeader(HCP_AUTH_HEADER_KEY, auth);

        //execute DELETE request
        HttpResponse response = httpClient.execute(request);

        //print response status to console
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
    }
}

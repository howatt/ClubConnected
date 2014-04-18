package com.clubconnected.dj.Network;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;

/**
 * Modified by Howatt on 17/04/14.
 * Originally coded by Dmitri
 * Taken from the CIS sample project and modified.
 * Used to contact servers and return results.
 *
 */
public class httpHandler  {


    private InputStream is = null;
    private String rawData = "";


    // makes an http request via GET and returns a result.
    public httpHandler (String url) {

        // Making HTTP request
        try {
            // request method is GET
            DefaultHttpClient httpClient = new DefaultHttpClient();

            System.out.println(url);

            HttpGet httpGet = new HttpGet(url);
            System.out.println("1");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            System.out.println("2");
            HttpEntity httpEntity = httpResponse.getEntity();
            System.out.println("3");
            is = httpEntity.getContent();
            System.out.println("4");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            rawData = sb.toString();
            Log.e("Data Returned", rawData);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
    }


    // make the return data available in raw format
    public String getRawData() {
        return rawData;
    }

    // make the return data available via a JSON array
    public JSONArray getJsonArray() throws JSONException {
        return new JSONArray(rawData);
    }

}


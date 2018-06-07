package com.rssheap.utilities;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonRequest {
	
	private String urlStr;
	private Context context;
	private String secretGuid = "f123c52cd1e2442290c57f353250b232";
	private String version = "v1";
	
	public JsonRequest(Context context, String urlStr)
	{
		this.context = context;
		this.urlStr = "http://app.rssheap.com" + "/" + version + urlStr;
		//this.urlStr = "http://192.168.0.116/" + urlStr;
	}
	
	public JSONObject Post(JSONObject json) {
		if(json == null) json = new JSONObject();

		try {
			json.put("GUID", secretGuid);
			json.put("USERGUID", Utilities.getUserGUID(context));
			json.put("device", "android");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		BufferedReader reader = null;

		try {
			URL url = new URL(urlStr);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestMethod("POST");

			//To enable inputting values using POST method
			//(Basically, after this we can write the dataToSend to the body of POST method)
			con.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			//Writing dataToSend to outputstreamwriter
			writer.write(json.toString());
			//Sending the data to the server - This much is enough to send data to server
			writer.flush();

			StringBuilder sb = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String line;
			while((line = reader.readLine()) != null) {
				sb.append(line);
			}
			line = sb.toString();
			return new JSONObject(line);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
    }	
}

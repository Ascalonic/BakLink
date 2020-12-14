package com.ascalonic.baklink;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ClientData {
    public String Id;
    public List<Video> Videos;

    public ClientData()
    {
        Videos = new ArrayList<Video>();
    }

    public boolean ReadFromFile(Context context)
    {
        File file = context.getExternalFilesDir(null);
        File dataPath = new File(file , "data");
        if (!dataPath.exists()) {
            dataPath.mkdir();
        }
        File dataFile = new File(dataPath.getPath(), "client.json");
        if(!dataFile.exists())
            return false;

        try {
            String jsonString = getStringFromFile(dataFile);
            JSONObject obj = new JSONObject(jsonString);
            Id = obj.getString("id");
            JSONArray jArray = obj.getJSONArray("videos");
            for(int i=0;i<jArray.length();i++)
            {
                Video video = new Video();
                JSONObject videoObj = jArray.getJSONObject(i);
                video.Title = videoObj.getString("title");
                video.Url = videoObj.getString("url");
                video.Path = videoObj.getString("path");
                Videos.add(video);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private String getStringFromFile (File file) throws Exception {
        FileInputStream fin = new FileInputStream(file);
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;
    }

    public void WriteToFile(Context context)
    {
        try
        {
            JSONObject jObject = new JSONObject();
            jObject.put("id", Id);
            JSONArray jArray = new JSONArray();
            for(int i=0;i<Videos.size();i++)
            {
                JSONObject videoObj = new JSONObject();
                videoObj.put("title", Videos.get(i).Title);
                videoObj.put("url", Videos.get(i).Url);
                videoObj.put("path", Videos.get(i).Path);
                jArray.put(videoObj);
            }
            jObject.put("videos", jArray);
            String jsonString = jObject.toString();

            File file = context.getExternalFilesDir(null);
            File dataPath = new File(file , "data");
            if (!dataPath.exists()) {
                dataPath.mkdir();
            }
            File dataFile = new File(dataPath.getPath(), "client.json");
            try (PrintWriter out = new PrintWriter(dataFile)) {
                out.println(jsonString);
            }
        }
        catch (FileNotFoundException fex)
        {

        }
        catch(JSONException jex)
        {

        }
    }
}

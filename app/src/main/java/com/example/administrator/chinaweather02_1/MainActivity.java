package com.example.administrator.chinaweather02_1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements Runnable {
//    private EditText et_city;
//    private Button btn_chaxun;
//    private String result;
//    private TextView tv_result;

    //    private String weatherUrl="http://weather.com.cn/wmaps/xml/"+result+".xml";
//    private String weatherIcon="http://m.weather.com.cn/img/c";
//    URL url=new URL(weatherUrl);
//    //建立天气预报查询连接
//    httpConn=(HttpURLConnection)url.openConnection();
    private HttpURLConnection httpConn;
    private InputStream din;
    private Vector<String> cityname = new Vector<String>();
    private Vector<String> low = new Vector<String>();
    private Vector<String> high = new Vector<String>();
    private Vector<String> icon = new Vector<String>();
    private Vector<Bitmap> bitmap = new Vector<Bitmap>();
    private Vector<String> summary = new Vector<String>();
    private int weatherIndex[] = new int[20];
    String city = "guangzhou";
    boolean bPress = false;
    boolean bHasData = false;
    LinearLayout body;
    Button find;
    EditText value;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("天气查询");
        body = (LinearLayout) findViewById(R.id.reuslt);
        find = (Button) findViewById(R.id.btn_chaxun);
        value = (EditText) findViewById(R.id.et_city);

//        et_city=(EditText)findViewById(R.id.et_city);
//        btn_chaxun=(Button)findViewById(R.id.btn_chaxun);

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                body.removeAllViews();
                city = value.getText().toString();
                Toast.makeText(MainActivity.this, "正在查询天气信息...请稍等!", Toast.LENGTH_SHORT).show();
                Thread thread = new Thread(MainActivity.this);
                thread.start();
//tv_result.setText(linearLayout);
//               result=et_city.getText().toString();
//               tv_result.setText(result);
            }
        });

    }

    @Override
    public void run() {
        cityname.removeAllElements();
        low.removeAllElements();
        high.removeAllElements();
        icon.removeAllElements();
        bitmap.removeAllElements();
        summary.removeAllElements();


        parseData();   //获取数据
        downloadImage();//下载图片
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    public void parseData() {
        int i = 0;
        String sValue;
        String weatherUrl = "http://weather.com.cn/wmaps/xml/" + city + ".xml";
        String weatherIcon = "http://m.weather.com.cn/img/c";
        URL url = null;
        try {
            url = new URL(weatherUrl);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");//采用GET请求方法
            din = httpConn.getInputStream();
            //打开输入流
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(din, "UTF-8");
            int evtType = xmlParser.getEventType();
            while (evtType != XmlPullParser.END_DOCUMENT)/*一直循环，直到文档结束*/ {
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        String tag = xmlParser.getName();

                        if (tag.equalsIgnoreCase("city")) {
                            cityname.addElement(xmlParser.getAttributeValue(null, "cityname") + "天气:");
                            summary.addElement(xmlParser.getAttributeValue(null, "stateDetailed"));
                            low.addElement("最低:" + xmlParser.getAttributeValue(null, "tem2"));
                            high.addElement("最高:" + xmlParser.getAttributeValue(null, "tem1"));
                            icon.addElement(weatherIcon + xmlParser.getAttributeValue(null, "state1") + ".gif");
                        }

                        break;
                    case XmlPullParser.END_TAG:
                    default:
                        break;
                }
                evtType = xmlParser.next();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        //建立天气预报查询连接

    }

    public void downloadImage() {
        int i;
        for (i = 0; i < icon.size(); i++) {
            try {
                URL url = new URL(icon.elementAt(i));
                System.out.println(icon.elementAt(i));
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("GET");
                din = httpConn.getInputStream();
                //图片数据Bitmap
                bitmap.addElement(BitmapFactory.decodeStream(httpConn.getInputStream()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //释放连接
                try {
                    din.close();
                    httpConn.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

    }
//显示结果handler
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    showData();
                    break;
            }
            super.handleMessage(msg);
        }
    };
//显示结果
    public void showData() {
        body.removeAllViews();
        body.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 80;
        params.height = 50;
        for (int i = 0; i < cityname.size(); i++) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            //城市
            TextView dayView = new TextView(this);
            dayView.setLayoutParams(params);
            dayView.setText(cityname.elementAt(i));
            linearLayout.addView(dayView);
            //描述
            TextView summaryView = new TextView(this);
            summaryView.setLayoutParams(params);
            summaryView.setText(summary.elementAt(i));
            linearLayout.addView(summaryView);
            //图标
            ImageView icon = new ImageView(this);
            icon.setLayoutParams(params);
            icon.setImageBitmap(bitmap.elementAt(i));
            linearLayout.addView(icon);
            //最低气温
            TextView lowView = new TextView(this);
            lowView.setLayoutParams(params);
            lowView.setText(low.elementAt(i));
            linearLayout.addView(lowView);
            //最高气温
            TextView highView = new TextView(this);
            lowView.setLayoutParams(params);
            lowView.setText(high.elementAt(i));
            linearLayout.addView(highView);
            body.addView(linearLayout);
        }
    }
}
/*
    class connectWeatherServer extends  Thread{
        JsonDemo activity;
        String sUrl;
        public connectWeatherServer(JsonDemo activity,String sUrl){
            this.activity=activity;
            this.sUrl=sUrl;
        }
        @Override
        public void run(){
            showWeatherJSON();

        }
        public void showWeatherJSON(){
            try {
                URL url=new URL(sUrl);
                HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                InputStream din=urlConnection.getInputStream();
                InputStreamReader isr=new InputStreamReader(urlConnection.getInputStream());
                BufferedReader bufferedReader=new BufferedReader(isr);
                String inputLine=null;
                //使用循环来获取得的数据
                while((inputLine=bufferedReader.readLine())!=null){
                    JsonData.append(inputLine);
                }
                String sJsonData=JsonData.toString();
                JSONObject jsonObject=new JSONObject(sJsonData);
                JSONObject cityweather=jsonObject.getJSONObject("weatherinfo");
                StringBuffer weatherInfo=new StringBuffer();
                weatherInfo.append("城市:"+cityweather.getString("city"));
                weatherInfo.append("天气情况:"+cityweather.getString("weather"));
                weatherInfo.append("最高温度:"+cityweather.getString("temp1"));
                weatherInfo.append("最低温度:"+cityweather.getString("temp2"));
                Message message=new Message();
                message.what=1;
                message.obj=weatherInfo;
                activity.handler.sendMessage(message);
                //关闭输入流
                bufferedReader.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        public boolean onCreateOptionsMenu(Menu menu)
//        {
//            menu.add(1,2,1,"读取JSON天气预报数据");
//
//        }


    }
*/


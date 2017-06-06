package com.example.android.restapipractice;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.restapipractice.entities.Example;
import com.example.android.restapipractice.entities.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName() + "_TAG";
    private static final String BASE_URL = "https://randomuser.me/api";
    private static final String MESSAGE_RESPONSE_CODE = "com.example.android.RESTApiPractice.MESSAGE_RESPONSE_CODE";
    private static final String MESSAGE_RESPONSE = "com.example.android.RESTApiPractice.MESSAGE_RESPONSE";
    private static final String MESSAGE_BODY = "com.example.android.RESTApiPractice.MESSAGE_BODY";
    private static final String RETROFIT_BASE_URL ="https://randomuser.me/";

    Button nativeBtn;
    Button okHttpBtn;
    Button retrofitBtn;
    Button clearBtn;

    TextView responseCodeTv;
    TextView responseMsgTv;
    TextView responseBodyTv;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setTextViews(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nativeBtn = (Button) findViewById(R.id.nativeReqBtn);
        okHttpBtn = (Button) findViewById(R.id.okHttpReqBtn);
        retrofitBtn = (Button) findViewById(R.id.retrofitReqBtn);
        clearBtn = (Button) findViewById(R.id.clearBtn);

        nativeBtn.setOnClickListener(this);
        okHttpBtn.setOnClickListener(this);
        retrofitBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);

        responseCodeTv = (TextView) findViewById(R.id.responseCodeTv);
        responseMsgTv = (TextView) findViewById(R.id.responseMsgTv);
        responseBodyTv = (TextView) findViewById(R.id.resultTv);
    }

    //Native way: using HTTPUrlConnection
    private void doNativeNetworkCall() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String body = "";

                    URL url = new URL(BASE_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    int responseCode = connection.getResponseCode();
                    //typically, use the response code and put it in a switch statment to handle different responses.

                    String response = connection.getResponseMessage();
                    Log.d(TAG, "doNativeNetworkCall: Response code: " + responseCode + " Message: " + response);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;

                    while((inputLine = reader.readLine()) != null) {
                        body += inputLine;
                    }

                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putInt(MESSAGE_RESPONSE_CODE, responseCode);
                    b.putString(MESSAGE_RESPONSE, response);
                    b.putString(MESSAGE_BODY, body);
                    msg.setData(b);

                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    //Using OkHttp library
    private void doOkHttpNetworkCall() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(BASE_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //handle internet access failure
                //handle retry policies
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final int code = response.code();
                    final String msg = response.message();
                    final String body = response.body().toString();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            postResult(code, msg, body);
                        }
                    });
                } else {
                    //error
                }
            }
        });
    }

    //Using Retrofit library -- most common
    private void doRetrofitNetworkCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RETROFIT_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitService service = retrofit.create(RetrofitService.class);
        retrofit2.Call<Example> call = service.getExampleUser();
        call.enqueue(new retrofit2.Callback<Example>() {
            @Override
            public void onResponse(retrofit2.Call<Example> call, retrofit2.Response<Example> response) {
                if(response.isSuccessful()) {
                    Example example = response.body();

                    for(Result r: example.getResults()) {
                        Log.d(TAG, "onResponse: Name = " + r.getName());
                        Log.d(TAG, "onResponse: Id = " + r.getId());
                        Log.d(TAG, "onResponse: Location = " + r.getLocation());
                        Log.d(TAG, "onResponse: Login = " + r.getLogin());
                        Log.d(TAG, "onResponse: Picture = " + r.getPicture());
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Example> call, Throwable t) {
                //do stuff on failure
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void setTextViews(Message message) {
        postResult(message.getData().getInt(MESSAGE_RESPONSE_CODE),
                message.getData().getString(MESSAGE_RESPONSE),
                message.getData().getString(MESSAGE_BODY));
    }

    private void postResult(int code, String msg, String body) {
        responseCodeTv.setText(String.format(getString(R.string.lbl_result_code),code ));
        responseMsgTv.setText(String.format(getString(R.string.lbl_result_msg), msg));
        responseBodyTv.setText(String.format(getString(R.string.lbl_result_body), body));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nativeReqBtn:
                doNativeNetworkCall();
                break;
            case R.id.okHttpReqBtn:
                doOkHttpNetworkCall();
                break;
            case R.id.retrofitReqBtn:
                doRetrofitNetworkCall();
                break;
            case R.id.clearBtn:
                clearTextViews();
                break;
        }
    }

    private void clearTextViews() {
        responseCodeTv.setText("");
        responseMsgTv.setText("");
        responseBodyTv.setText("");
    }
}

package com.example.android.restapipractice;

import com.example.android.restapipractice.entities.Example;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Android on 6/6/2017.
 */

public interface RetrofitService {

    @GET("api")
    Call<Example> getExampleUser();

}

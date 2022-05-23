package com.example.staggeredgridexample.Retrofit

import com.example.staggeredgridexample.Objects.MergeObject
import com.example.staggeredgridexample.Objects.SearchObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {
    @GET("videoKeywordSearch.php")
    fun searchKeyword(
        @HeaderMap headers: HashMap<String, String>,
        @Query("keyword") keyword: String
    ): Call<SearchObject>

    @POST
    fun selectedVideoSend(
        @Url urlValue: String,
        @HeaderMap headers: HashMap<String, String>,
        @Body body: RequestBody
    ) : Call<MergeObject>
}
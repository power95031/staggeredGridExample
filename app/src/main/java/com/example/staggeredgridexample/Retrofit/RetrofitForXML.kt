package com.example.staggeredgridexample.Retrofit

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class RetrofitForXML(url:String) {
    var retrofit: Retrofit
    var retrofitService: RetrofitService
    val service: RetrofitService
        get() = retrofitService


    init {
        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }
}
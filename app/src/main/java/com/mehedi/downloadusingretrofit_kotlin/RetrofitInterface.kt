package com.mehedi.downloadusingretrofit_kotlin

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming

interface RetrofitInterface {

    @GET("storage/feaade38c1651bd01984236/2017/04/file_example_MP4_1920_18MG.mp4")
    @Streaming
    fun downloadFile(): Call<ResponseBody>

}
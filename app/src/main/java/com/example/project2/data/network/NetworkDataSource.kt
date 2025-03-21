package com.example.project2.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


interface NetworkDataSource {
//    @GET("api/generate")
//    suspend fun getNetworkChatItem(): List<NetworkChatItem>

    @POST("api/generate")
    suspend fun generateChat(@Body request: ChatRequest): NetworkChatItem
    //suspend fun pushNetworkChatItem(): List<NetworkChatItem>
}
//'http://172.25.54.61:11434/api/generate'
object ChatAPI{
    private const val BASE_URL = "https://207.148.111.64/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<javax.net.ssl.X509TrustManager>(
            object : javax.net.ssl.X509TrustManager { // 绕过ssh证书，这部分还没弄好
                override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }
        )

        val sslContext = javax.net.ssl.SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }

        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0])
            .hostnameVerifier { _, _ -> true }  // 跳过 Hostname 验证
            .connectTimeout(30, TimeUnit.SECONDS) // 连接超时 30s
            .readTimeout(300, TimeUnit.SECONDS)    // 读取超时 60s
            .writeTimeout(300, TimeUnit.SECONDS)   // 写入超时 60s
            .build()
    }

    val retrofitService :NetworkDataSource by lazy{
        retrofit.create(NetworkDataSource::class.java)
    }
}
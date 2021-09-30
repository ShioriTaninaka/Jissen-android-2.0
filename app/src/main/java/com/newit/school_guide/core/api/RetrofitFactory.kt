package com.newit.school_guide.core.api

import com.newit.school_guide.BuildConfig
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitFactory {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val trustAllCerts =
        arrayOf<TrustManager>(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                fun checkServerTrusted(
                    arr: Array<X509Certificate>,
                    authType: String,
                    host: String
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
    // Install the all-trusting trust manager
    private var sslContext: SSLContext =
        SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }

    // Create an ssl socket factory with our all-trusting manager
    private val sslSocketFactory = sslContext.socketFactory

    private val client by lazy {
        OkHttpClient().newBuilder()
            .readTimeout(30,TimeUnit.SECONDS)
            .writeTimeout(30,TimeUnit.SECONDS)
            .connectTimeout(30,TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
//            .addInterceptor {chain ->
//                val original: Request = chain.request()
//
//                val builder = original.newBuilder()
//                if(CommonSharedPreferences.getInstance().getBoolean(Constants.IS_LOGIN)){
//                    builder.addHeader("Authorization","Bearer " + CommonSharedPreferences.getInstance().getString(
//                        Constants.ACCESS_TOKEN))
//                }
//                val request: Request = builder.build()
//                chain.proceed(request)
//            }

//            .sslSocketFactory(
//                sslSocketFactory,
//                (trustAllCerts[0] as X509TrustManager)
//            )
            .build()
    }

    val api: Api = Retrofit.Builder()
        .client(client)
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(Api::class.java)
}

suspend fun network(): Api = withContext(Dispatchers.IO) {
    RetrofitFactory.api
}
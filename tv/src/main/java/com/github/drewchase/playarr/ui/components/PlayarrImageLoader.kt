package com.github.drewchase.playarr.ui.components

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.OkHttpClient

fun createPlayarrImageLoader(context: Context, authToken: String?): ImageLoader {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            if (!authToken.isNullOrBlank()) {
                requestBuilder.addHeader("Cookie", "plex_user_token=$authToken")
            }
            chain.proceed(requestBuilder.build())
        }
        .build()

    return ImageLoader.Builder(context)
        .components {
            add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
        }
        .build()
}

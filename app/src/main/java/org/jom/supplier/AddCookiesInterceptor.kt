package org.jom.supplier

import okhttp3.Interceptor
import okhttp3.Response

class AddCookiesInterceptor(private val cookies: Map<String, String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        for ((key, value) in cookies) {
            requestBuilder.addHeader("Cookie", "$key=$value")
        }

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}
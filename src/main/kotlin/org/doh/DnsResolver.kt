package org.doh

import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.doh.Constants.Companion.ACCEPT
import org.doh.Constants.Companion.APPLICATION_DNS_JSON
import org.doh.Constants.Companion.NAME
import org.doh.Constants.Companion.TYPE
import org.doh.pojo.DnsQuery
import org.doh.pojo.DnsResponse
import java.util.concurrent.TimeUnit

abstract class DnsResolver(
    private val client: OkHttpClient = defaultClient
) {
    companion object {
        val defaultClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<DnsResponse> = moshi.adapter(DnsResponse::class.java)
    }

    abstract fun getResolverUrl(): String

    fun resolve(query: DnsQuery): DnsResponse? {
        val httpUrl = getResolverUrl().toHttpUrlOrNull()
            ?: throw IllegalStateException("Invalid resolver URL: ${getResolverUrl()}")
        val url = httpUrl.newBuilder()
            .addQueryParameter(NAME, query.name)
            .addQueryParameter(TYPE, query.type.toString())
        return callApi(url.toString())
    }

    fun resolve(name: String, type: String): DnsResponse? {
        val httpUrl = getResolverUrl().toHttpUrlOrNull()
            ?: throw IllegalStateException("Invalid resolver URL: ${getResolverUrl()}")
        val url = httpUrl.newBuilder()
            .addQueryParameter(NAME, name)
            .addQueryParameter(TYPE, type)
        return callApi(url.toString())
    }

    private fun callApi(url: String): DnsResponse? {
        val request = Request.Builder()
            .url(url)
            .addHeader(ACCEPT, APPLICATION_DNS_JSON)
            .build();

        val response = client.newCall(request).execute()
        if (response.code == 200) {
            val body = response.body?.string()
            return body?.takeIf { it.isNotBlank() }?.let { jsonAdapter.fromJson(it) }
        }

        return null
    }
}

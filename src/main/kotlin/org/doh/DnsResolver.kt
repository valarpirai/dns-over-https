package org.doh

import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.doh.Constants.Companion.ACCEPT
import org.doh.Constants.Companion.APPLICATION_DNS_JSON
import org.doh.Constants.Companion.NAME
import org.doh.Constants.Companion.TYPE
import org.doh.pojo.DnsQuery
import org.doh.pojo.DnsResponse

abstract class DnsResolver {
    private val client = OkHttpClient()
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter: JsonAdapter<DnsResponse> = moshi.adapter<DnsResponse>(DnsResponse::class.java)

    abstract fun getResolverUrl(): String

    fun resolve(query: DnsQuery): DnsResponse? {
        val httpUrl = getResolverUrl().toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter(NAME, query.name)
            .addQueryParameter(TYPE, query.type.toString())
        return callApi(httpUrl.toString())
    }

    fun resolve(name: String, type: String): DnsResponse? {
        val httpUrl = getResolverUrl().toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter(NAME, name)
            .addQueryParameter(TYPE, type)
        return callApi(httpUrl.toString())
    }

    private fun callApi(url: String): DnsResponse? {
        val request = Request.Builder()
            .url(url)
            .addHeader(ACCEPT, APPLICATION_DNS_JSON)
            .build();

        val response = client.newCall(request).execute()
        if (response.code == 200) {
            val body = response.body?.string()
            return body?.let { jsonAdapter.fromJson(it) }
        }

        return null
    }
}

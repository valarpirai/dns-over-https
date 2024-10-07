package org.doh

import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.doh.Constants.Companion.ACCEPT
import org.doh.Constants.Companion.APPLICATION_DNS_JSON
import org.doh.pojo.DnsQuery
import org.doh.pojo.DnsResponse

abstract class DnsResolver {
    private val client = OkHttpClient()
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter: JsonAdapter<DnsResponse> = moshi.adapter<DnsResponse>(DnsResponse::class.java)

    abstract fun getResolverUrl(): String

    fun resolve(query: DnsQuery): DnsResponse? {
        val url = "${getResolverUrl()}?type=${query.type.toString()}&name=${query.name}"
        return callApi(url)
    }

    fun resolve(name: String, type: String): DnsResponse? {
        val url = "${getResolverUrl()}?type=${type}&name=${name}"
        return callApi(url)
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

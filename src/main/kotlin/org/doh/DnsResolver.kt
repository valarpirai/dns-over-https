package org.doh

import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.doh.Constants.Companion.ACCEPT
import org.doh.Constants.Companion.APPLICATION_DNS_JSON
import org.doh.pojo.DnsQuery
import org.doh.pojo.DnsResponse

abstract class DnsResolver {
    private val client = OkHttpClient()
    private val moshi: Moshi = Moshi.Builder().build()
    private val jsonAdapter: JsonAdapter<DnsResponse> = moshi.adapter<DnsResponse>(DnsResponse::class.java)

    abstract fun getResolverUrl(): String

    private fun buildURL(query: DnsQuery): String {
        return "${getResolverUrl()}?type=${query.type.toString()}&name=${query.name}"
    }

    fun resolve(query: DnsQuery): DnsResponse? {
        val url = buildURL(query)
        val request = Request.Builder()
            .url(url)
            .addHeader(ACCEPT, APPLICATION_DNS_JSON)
            .build();

        val response = client.newCall(request).execute()
        if (response.code == 200) {
            response.body?.let { return jsonAdapter.fromJson(it.string()) }
        } else {
            return null
        }
    }
}

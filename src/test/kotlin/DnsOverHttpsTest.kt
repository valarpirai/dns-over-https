import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.doh.CloudFlareDnsResolver
import org.doh.GoogleDnsResolver
import org.doh.pojo.DnsQuery
import org.doh.pojo.DnsResponse
import org.doh.pojo.RecordType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DnsOverHttpsTest {

    @Test
    fun test_cloudFlareResolver() {
        val resolver = CloudFlareDnsResolver()
        val query = DnsQuery("google.com", RecordType.A)
        val response = resolver.resolve(query)!!
        Assertions.assertTrue(response.Question.isNotEmpty())
        Assertions.assertEquals("google.com", response.Question.first().name)
        Assertions.assertEquals("google.com", response.Answer?.first()?.name)
        Assertions.assertEquals(RecordType.A.type, response.Answer?.first()?.type)
    }

    @Test
    fun test_cloudFlareResolver_other_types() {
        val resolver = CloudFlareDnsResolver()
        val response = resolver.resolve("google.com", "A")!!
        Assertions.assertTrue(response.Question.isNotEmpty())
        Assertions.assertEquals("google.com", response.Question.first().name)
        Assertions.assertEquals("google.com", response.Answer?.first()?.name)
        Assertions.assertEquals(RecordType.A.type, response.Answer?.first()?.type)
    }

    @Test
    fun test_cloudFlareResolver_unknown_type() {
        val resolver = CloudFlareDnsResolver()
        val response = resolver.resolve("google.com", "test")
        Assertions.assertNull(response)
    }

    @Test
    fun test_cloudFlareResolver_CNAME() {
        val resolver = CloudFlareDnsResolver()
        val query = DnsQuery("google.com", RecordType.CNAME)
        val response = resolver.resolve(query)!!
        Assertions.assertTrue(response.Question.isNotEmpty())
        Assertions.assertEquals("google.com", response.Question.first().name)
        Assertions.assertNull(response.Answer)
        Assertions.assertEquals("google.com", response.Authority?.first()?.name)
        Assertions.assertEquals(RecordType.SOA.type, response.Authority?.first()?.type)
    }

    @Test
    fun test_cloudFlareResolver_SOA() {
        val resolver = CloudFlareDnsResolver()
        val query = DnsQuery("google.com", RecordType.SOA)
        val response = resolver.resolve(query)!!
        Assertions.assertTrue(response.Question.isNotEmpty())
        Assertions.assertEquals("google.com", response.Question.first().name)
        Assertions.assertEquals("google.com", response.Answer?.first()?.name)
        Assertions.assertEquals(RecordType.SOA.type, response.Answer?.first()?.type)
    }

    @Test
    fun test_cloudFlareResolver_all_types() {
        val types = listOf(RecordType.A, RecordType.AAAA, RecordType.MX, RecordType.TXT, RecordType.NS, RecordType.SOA)

        types.forEach {
            val resolver = CloudFlareDnsResolver()
            val query = DnsQuery("google.com", it)
            val response = resolver.resolve(query)!!
            Assertions.assertTrue(response.Question.isNotEmpty())
            Assertions.assertEquals("google.com", response.Question.first().name)
            Assertions.assertEquals("google.com", response.Answer?.first()?.name)
            Assertions.assertEquals(it.type, response.Answer?.first()?.type)
        }
    }

    @Test
    fun test_GoogleResolver_All_types() {
        val types = listOf(RecordType.A, RecordType.AAAA, RecordType.MX, RecordType.TXT, RecordType.NS, RecordType.SOA)

        types.forEach {
            val resolver = GoogleDnsResolver()
            val query = DnsQuery("google.com", it)
            val response = resolver.resolve(query)!!
            Assertions.assertTrue(response.Question.isNotEmpty())
            Assertions.assertEquals("google.com.", response.Question.first().name)
            Assertions.assertEquals("google.com.", response.Answer?.first()?.name)
            Assertions.assertEquals(it.type, response.Answer?.first()?.type)
        }
    }

    @Test
    fun test_GoogleResolver_unknown_type() {
        val resolver = GoogleDnsResolver()
        val response = resolver.resolve("google.com", "test")
        Assertions.assertNull(response)
    }

    @Test
    fun test_JsonParsing() {
        val body = """
            {"Status":10,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,"Question":[{"name":"google.com","type":1}],"Answer":[{"name":"google.com","type":1,"TTL":50,"data":"142.250.193.110"}]}
        """.trimIndent()

        val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<DnsResponse> = moshi.adapter<DnsResponse>(DnsResponse::class.java)
        val parsedResponse = jsonAdapter.fromJson(body)

        Assertions.assertNotNull(parsedResponse)
        Assertions.assertEquals(parsedResponse?.Status, 10)
        Assertions.assertEquals(parsedResponse?.Question?.size, 1)
    }
}
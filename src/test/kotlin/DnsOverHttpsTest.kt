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
        Assertions.assertEquals(response.Question.first().name, "google.com")
        Assertions.assertEquals(response.Answer?.first()?.name, "google.com")
        Assertions.assertEquals(response.Answer?.first()?.type, RecordType.A.type)
    }

    @Test
    fun test_cloudFlareResolver_MX() {
        val resolver = CloudFlareDnsResolver()
        val query = DnsQuery("google.com", RecordType.MX)
        val response = resolver.resolve(query)!!
        Assertions.assertTrue(response.Question.isNotEmpty())
        Assertions.assertEquals(response.Question.first().name, "google.com")
        Assertions.assertEquals(response.Answer?.first()?.name, "google.com")
        Assertions.assertEquals(response.Answer?.first()?.type, RecordType.MX.type)
    }

    @Test
    fun test_GoogleFlareResolver() {
        val resolver = GoogleDnsResolver()
        val query = DnsQuery("google.com", RecordType.A)
        val response = resolver.resolve(query)!!
        Assertions.assertTrue(response.Question.isNotEmpty())
        Assertions.assertEquals(response.Question.first().name, "google.com.")
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
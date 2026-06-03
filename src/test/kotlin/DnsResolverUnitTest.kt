import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.doh.DnsResolver
import org.doh.pojo.DnsQuery
import org.doh.pojo.DnsResponse
import org.doh.pojo.RecordType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DnsResolverUnitTest {

    private lateinit var server: MockWebServer
    private lateinit var resolver: DnsResolver

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
        val baseUrl = server.url("/dns-query").toString()
        resolver = object : DnsResolver() {
            override fun getResolverUrl() = baseUrl
        }
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun resolve_returns_parsed_response_on_200() {
        val body = """{"Status":0,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,
            |"Question":[{"name":"example.com","type":1}],
            |"Answer":[{"name":"example.com","type":1,"TTL":300,"data":"93.184.216.34"}]}
        """.trimMargin()
        server.enqueue(MockResponse().setResponseCode(200).setBody(body)
            .addHeader("Content-Type", "application/dns-json"))

        val response = resolver.resolve(DnsQuery("example.com", RecordType.A))

        Assertions.assertNotNull(response)
        Assertions.assertEquals(0, response!!.Status)
        Assertions.assertEquals("example.com", response.Question.first().name)
        Assertions.assertEquals("93.184.216.34", response.Answer!!.first().data)
    }

    @Test
    fun resolve_returns_null_on_non_200() {
        server.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

        val response = resolver.resolve("example.com", "INVALID")

        Assertions.assertNull(response)
    }

    @Test
    fun resolve_sends_correct_query_parameters() {
        val body = """{"Status":0,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,
            |"Question":[{"name":"example.com","type":28}],"Answer":null}
        """.trimMargin()
        server.enqueue(MockResponse().setResponseCode(200).setBody(body)
            .addHeader("Content-Type", "application/dns-json"))

        resolver.resolve(DnsQuery("example.com", RecordType.AAAA))

        val request = server.takeRequest()
        Assertions.assertTrue(request.requestUrl?.queryParameter("name") == "example.com")
        Assertions.assertTrue(request.requestUrl?.queryParameter("type") == "AAAA")
        Assertions.assertEquals("application/dns-json", request.getHeader("Accept"))
    }

    @Test
    fun resolve_throws_on_invalid_url() {
        val badResolver = object : DnsResolver() {
            override fun getResolverUrl() = "not a valid url"
        }
        Assertions.assertThrows(IllegalStateException::class.java) {
            badResolver.resolve("example.com", "A")
        }
    }

    @Test
    fun jsonAdapter_parses_response_with_authority() {
        val body = """{"Status":0,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,
            |"Question":[{"name":"example.com","type":5}],
            |"Authority":[{"name":"example.com","type":6,"TTL":900,"data":"ns1.example.com"}]}
        """.trimMargin()

        val response: DnsResponse? = DnsResolver.jsonAdapter.fromJson(body)

        Assertions.assertNotNull(response)
        Assertions.assertNull(response!!.Answer)
        Assertions.assertEquals("ns1.example.com", response.Authority!!.first().data)
    }
}

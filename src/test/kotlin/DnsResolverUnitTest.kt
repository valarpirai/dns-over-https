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

    @Test
    fun resolve_string_overload_sends_correct_query_parameters() {
        val body = """{"Status":0,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,
            |"Question":[{"name":"example.com","type":15}],"Answer":null}
        """.trimMargin()
        server.enqueue(MockResponse().setResponseCode(200).setBody(body)
            .addHeader("Content-Type", "application/dns-json"))

        resolver.resolve("example.com", "MX")

        val request = server.takeRequest()
        Assertions.assertEquals("example.com", request.requestUrl?.queryParameter("name"))
        Assertions.assertEquals("MX", request.requestUrl?.queryParameter("type"))
    }

    @Test
    fun resolve_returns_null_on_empty_body() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("")
            .addHeader("Content-Type", "application/dns-json"))

        val response = resolver.resolve("example.com", "A")

        Assertions.assertNull(response)
    }

    @Test
    fun resolve_throws_on_invalid_url_via_dnsquery_overload() {
        val badResolver = object : DnsResolver() {
            override fun getResolverUrl() = "not a valid url"
        }
        Assertions.assertThrows(IllegalStateException::class.java) {
            badResolver.resolve(DnsQuery("example.com", RecordType.A))
        }
    }

    @Test
    fun jsonAdapter_parses_response_with_additional_section() {
        val body = """{"Status":0,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,
            |"Question":[{"name":"example.com","type":1}],
            |"Answer":[{"name":"example.com","type":1,"TTL":300,"data":"93.184.216.34"}],
            |"Additional":[{"name":"ns1.example.com","type":1,"TTL":3600,"data":"205.251.196.1"}]}
        """.trimMargin()

        val response: DnsResponse? = DnsResolver.jsonAdapter.fromJson(body)

        Assertions.assertNotNull(response)
        Assertions.assertEquals("205.251.196.1", response!!.Additional!!.first().data)
    }

    @Test
    fun jsonAdapter_parses_response_with_comment_field() {
        val body = """{"Status":3,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,
            |"Question":[{"name":"nonexistent.example","type":1}],
            |"Comment":"NXDOMAIN"}
        """.trimMargin()

        val response: DnsResponse? = DnsResolver.jsonAdapter.fromJson(body)

        Assertions.assertNotNull(response)
        Assertions.assertEquals(3, response!!.Status)
        Assertions.assertEquals("NXDOMAIN", response.Comment)
        Assertions.assertNull(response.Answer)
    }

    @Test
    fun jsonAdapter_parses_response_with_empty_answer_list() {
        val body = """{"Status":0,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,
            |"Question":[{"name":"example.com","type":1}],
            |"Answer":[]}
        """.trimMargin()

        val response: DnsResponse? = DnsResolver.jsonAdapter.fromJson(body)

        Assertions.assertNotNull(response)
        Assertions.assertNotNull(response!!.Answer)
        Assertions.assertTrue(response.Answer!!.isEmpty())
    }

    @Test
    fun resolve_all_record_types_send_correct_type_string() {
        val cases = mapOf(
            RecordType.A to "A",
            RecordType.NS to "NS",
            RecordType.CNAME to "CNAME",
            RecordType.SOA to "SOA",
            RecordType.PTR to "PTR",
            RecordType.MX to "MX",
            RecordType.TXT to "TXT",
            RecordType.AAAA to "AAAA",
            RecordType.SRV to "SRV",
            RecordType.DS to "DS",
            RecordType.TLSA to "TLSA",
            RecordType.CAA to "CAA"
        )
        val emptyBody = """{"Status":0,"TC":false,"RD":true,"RA":true,"AD":false,"CD":false,
            |"Question":[{"name":"example.com","type":1}],"Answer":null}
        """.trimMargin()

        cases.forEach { (recordType, expectedTypeString) ->
            server.enqueue(MockResponse().setResponseCode(200).setBody(emptyBody)
                .addHeader("Content-Type", "application/dns-json"))
            resolver.resolve(DnsQuery("example.com", recordType))
            val request = server.takeRequest()
            Assertions.assertEquals(expectedTypeString, request.requestUrl?.queryParameter("type"),
                "RecordType.$recordType should send type=$expectedTypeString")
        }
    }
}

import org.doh.CloudFlareDnsResolver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DnsOverHttpsTest {

    @Test
    fun test_DnsQuery() {
        Assertions.assertTrue(CloudFlareDnsResolver.dnsResolverUrl)
    }
}
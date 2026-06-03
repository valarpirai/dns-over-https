import org.doh.CloudFlareDnsResolver
import org.doh.Constants
import org.doh.GoogleDnsResolver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DnsResolverUrlTest {

    @Test
    fun cloudflare_resolver_returns_correct_url() {
        val resolver = CloudFlareDnsResolver()
        Assertions.assertEquals(Constants.CLOUD_FLARE_DNS_RESOLVER_URL, resolver.getResolverUrl())
    }

    @Test
    fun google_resolver_returns_correct_url() {
        val resolver = GoogleDnsResolver()
        Assertions.assertEquals(Constants.GOOGLE_DNS_RESOLVER_URL, resolver.getResolverUrl())
    }

    @Test
    fun cloudflare_url_is_valid_https() {
        val url = Constants.CLOUD_FLARE_DNS_RESOLVER_URL
        Assertions.assertTrue(url.startsWith("https://"), "Cloudflare URL must use HTTPS")
    }

    @Test
    fun google_url_is_valid_https() {
        val url = Constants.GOOGLE_DNS_RESOLVER_URL
        Assertions.assertTrue(url.startsWith("https://"), "Google URL must use HTTPS")
    }
}

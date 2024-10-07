# dnsjava
DNS over HTTPS

## Why?
DNS client using DNS over HTTPS.

- supports almost all defined record types (including the DNSSEC types), and unknown types
- can be used for queries

## Getting started
Now we support two DNS resolvers
- CloudFlare (1.1.1.1) `CloudFlareDnsResolver`
- Google (8.8.8.8) `GoogleDnsResolver`


Fetch DNS A(IPv4) Record for `google.com` using DnsQuery. Create DnsQuery and resolve the IP.
```
    val resolver = CloudFlareDnsResolver()
    val query = DnsQuery("google.com", RecordType.A)
    val response = resolver.resolve(query)
    Assertions.assertNull(response)
```

Fetch DNS A(IPv4) Record for `google.com` without using DnsQuery. Directly pass String args and resolve the IPs
```
    val resolver = CloudFlareDnsResolver()
    val response = resolver.resolve("google.com", "A")
    Assertions.assertNull(response)
```

The resolve method will return `DnsResponse` for success response or `null` for error response

### How it works?
- https://developers.cloudflare.com/1.1.1.1/encryption/dns-over-https/
- https://developers.google.com/speed/public-dns/docs/doh

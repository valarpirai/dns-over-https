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

### Request
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

### Response
The `resolve` method will return `DnsResponse` for success response or `null` for error response

The `DnsResponse` will have the following fields
| Field | Comment |
| ------------- | ------------- |
| List of `Question` -> `name` and `type` | Contains the query |
| List of `Answer` -> `name`, `type`, `TTL` and `data` | Contains the respective response if exists. Otherwise null |
| List of `Authority`  -> `name`, `type`, `TTL` and `data` | If the Answer is null, then Authority (SOA) server details will be sent in the response |

Example Curl request
```
curl --location 'https://1.1.1.1/dns-query?type=A&name=amazon.com' \
--header 'Accept: application/dns-json'

{
    "Status": 0,
    "TC": false,
    "RD": true,
    "RA": true,
    "AD": false,
    "CD": false,
    "Question": [
        {
            "name": "amazon.com",
            "type": 1
        }
    ],
    "Answer": [
        {
            "name": "amazon.com",
            "type": 1,
            "TTL": 435,
            "data": "54.239.28.85"
        },
        {
            "name": "amazon.com",
            "type": 1,
            "TTL": 435,
            "data": "52.94.236.248"
        },
        {
            "name": "amazon.com",
            "type": 1,
            "TTL": 435,
            "data": "205.251.242.103"
        }
    ]
}
```

### For more details please refer the following links
- https://developers.cloudflare.com/1.1.1.1/encryption/dns-over-https/
- https://developers.google.com/speed/public-dns/docs/doh

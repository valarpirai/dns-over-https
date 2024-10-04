package org.doh.pojo

data class DnsQuery(val name: String, val type: RecordType) {}

enum class RecordType {
    NS,
    A,
    AAAA,
    MX,
    CNAME,
    TXT,
    DMARC,
    SOA
}
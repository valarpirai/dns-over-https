package org.doh.pojo

data class DnsQuery(val name: String, val type: RecordType) {}

enum class RecordType(val type: Int) {
    A(1),
    NS(2),
    CNAME(5),
    SOA(6),
    MX(15),
    TXT(16),
    AAAA(28),
    PTR(12),
    SRV(33),
    DS(43),
    TLSA(52),
    CAA(257)
}
package org.doh.pojo

class DnsResponse {
    val Status: Int = 0
    val TC: Boolean = false
    val RD: Boolean = true
    val RA: Boolean = true
    val AD: Boolean = false
    val CD: Boolean = false

    val Question: List<Question>? = null
    val Answer: List<Answer>? = null
    val Authority: List<Authority>? = null
    val Additional: List<Additional>? = null
    val Comment: String? = null
}

data class Question(val name: String, val type: Int) {}
data class Answer(val name: String, val type: Int, val TTL: Int, val data: String) {}
data class Authority(val name: String, val type: Int, val TTL: Int, val data: String) {}
data class Additional(val name: String, val type: Int, val TTL: Int, val data: String) {}


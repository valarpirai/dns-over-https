import org.doh.pojo.RecordType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RecordTypeTest {

    @Test
    fun record_type_numeric_codes_match_rfc_1035_and_extensions() {
        Assertions.assertEquals(1, RecordType.A.type)
        Assertions.assertEquals(2, RecordType.NS.type)
        Assertions.assertEquals(5, RecordType.CNAME.type)
        Assertions.assertEquals(6, RecordType.SOA.type)
        Assertions.assertEquals(12, RecordType.PTR.type)
        Assertions.assertEquals(15, RecordType.MX.type)
        Assertions.assertEquals(16, RecordType.TXT.type)
        Assertions.assertEquals(28, RecordType.AAAA.type)
        Assertions.assertEquals(33, RecordType.SRV.type)
        Assertions.assertEquals(43, RecordType.DS.type)
        Assertions.assertEquals(52, RecordType.TLSA.type)
        Assertions.assertEquals(257, RecordType.CAA.type)
    }

    @Test
    fun record_type_toString_returns_name() {
        Assertions.assertEquals("A", RecordType.A.toString())
        Assertions.assertEquals("AAAA", RecordType.AAAA.toString())
        Assertions.assertEquals("MX", RecordType.MX.toString())
        Assertions.assertEquals("CAA", RecordType.CAA.toString())
    }

    @Test
    fun all_record_types_have_unique_numeric_codes() {
        val codes = RecordType.entries.map { it.type }
        Assertions.assertEquals(codes.size, codes.toSet().size, "Duplicate numeric codes found")
    }
}

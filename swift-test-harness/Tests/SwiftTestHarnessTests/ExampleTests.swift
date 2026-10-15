#if canImport(Testing)
import Testing
import RandDistr

@Suite("RandDistr Swift Export Tests")
struct RandDistrExportTests {
    @Test("RandDistr swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "RandDistr swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import RandDistr

final class RandDistrExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "RandDistr swift module imported cleanly")
    }
}
#endif

#if canImport(Testing)
import Testing
import RandDistr

@Suite("RandDistr Swift Export Tests")
struct RandDistrExportTests {
    @Test("RandDistr swift module imported cleanly and instantiates distributions")
    func testSwiftModuleLoads() throws {
        let normal = Normal.Companion.shared.new(mean: 0.0, stdDev: 1.0)
        #expect(normal.isOk(), "Normal distribution created successfully")
        let zipf = Zipf.Companion.shared.new(n: 10.0, s: 1.5)
        #expect(zipf.isOk(), "Zipf distribution created successfully")
    }
}
#elseif canImport(XCTest)
import XCTest
import RandDistr

final class RandDistrExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        let normal = Normal.Companion.shared.new(mean: 0.0, stdDev: 1.0)
        XCTAssertTrue(normal.isOk(), "Normal distribution created successfully")
        let zipf = Zipf.Companion.shared.new(n: 10.0, s: 1.5)
        XCTAssertTrue(zipf.isOk(), "Zipf distribution created successfully")
    }
}
#endif

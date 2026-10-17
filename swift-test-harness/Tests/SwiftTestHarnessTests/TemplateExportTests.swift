import XCTest
import RandDistr

final class TemplateExportTests: XCTestCase {
    func testModuleImportsAndLinks() {
        let normal = Normal.Companion.shared.new(mean: 0.0, stdDev: 1.0)
        XCTAssertTrue(normal.isOk())
    }
}

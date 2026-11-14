import XCTest

final class ColorInvoControlInteractionUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    override func tearDownWithError() throws {
        app?.terminate()
        app = nil
    }

    func testWallpaperPaletteControlsStayInteractiveAcrossStyles() {
        launchShowcaseApp()

        for style in ["貓貓", "顏料", "極簡"] {
            selectDecoration(style)

            for index in [1, 2, 0] {
                perform(.palette(index))
            }

            assertCoreControlsHittable()
        }
    }

    func testColorPickersAcceptTapsAfterPaletteSwitching() {
        for identifier in ["backgroundColorPicker", "barColorPicker"] {
            launchShowcaseApp()

            perform(.palette(1))
            perform(.palette(2))
            perform(.palette(0))
            perform(.palette(2))

            let picker = control(identifier)
            XCTAssertTrue(picker.waitForExistence(timeout: 3), "\(identifier) should exist")
            scrollToHittable(picker)
            XCTAssertTrue(picker.isHittable, "\(identifier) should remain hittable")
            picker.tap()

            app.terminate()
        }
    }

    func testWallpaperPaletteControlsStayAvailableForEveryStyle() {
        launchShowcaseApp()

        openColors()
        let paletteButton = app.buttons["wallpaperPaletteOption.0"]
        XCTAssertTrue(
            paletteButton.waitForExistence(timeout: 3),
            "wallpaper palettes should exist"
        )
        XCTAssertTrue(paletteButton.isEnabled, "wallpaper palettes should be enabled for cat")

        selectDecoration("顏料")
        openColors()
        XCTAssertTrue(paletteButton.isEnabled, "wallpaper palettes should be enabled for paint")

        selectDecoration("極簡")
        openColors()
        XCTAssertTrue(paletteButton.isEnabled, "wallpaper palettes should be enabled for minimal")
    }

    func testSetupRequiresValidCarrierAndPreservesChoicesWhenGoingBack() {
        launchShowcaseApp()
        let field = app.textFields["carrierCodeField"]
        let next = app.buttons["setupNextButton"]
        field.tap()
        field.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: 7))
        XCTAssertFalse(next.isEnabled, "An empty carrier must not advance")
        field.typeText("ABC")
        XCTAssertFalse(next.isEnabled, "An incomplete carrier must not advance")
        field.typeText("1234")
        XCTAssertTrue(next.isEnabled)
        next.tap()
        XCTAssertFalse(field.exists)

        selectDecoration("極簡")
        openColors()
        perform(.palette(1))
        next.tap()
        XCTAssertTrue(app.staticTexts["加入主畫面"].waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["wallpaperPaletteOption.1"].exists)
        app.buttons["setupBackButton"].tap()
        XCTAssertEqual(app.buttons["wallpaperPaletteOption.1"].value as? String, "selected")
        app.buttons["setupBackButton"].tap()
        XCTAssertTrue(app.segmentedControls["decorationPicker"].buttons["極簡"].isSelected)
        app.buttons["setupBackButton"].tap()
        XCTAssertEqual(field.value as? String, "ABC1234")
    }

    private func launchShowcaseApp() {
        app?.terminate()
        app = XCUIApplication()
        app.launchArguments = ["--showcase-data", "-AppleLanguages", "(zh-Hant)"]
        app.launchEnvironment["COLORINVO_SHOWCASE_DATA"] = "1"
        app.launch()

        XCTAssertTrue(
            app.textFields["carrierCodeField"].waitForExistence(timeout: 8),
            "Carrier entry should be the first page"
        )
    }

    private func perform(_ operation: ControlOperation) {
        openColors()
        switch operation {
        case .palette:
            let button = app.buttons[operation.identifier]
            XCTAssertTrue(button.waitForExistence(timeout: 3), "\(operation.identifier) should exist")
            scrollToHittable(button)
            XCTAssertTrue(button.isHittable, "\(operation.identifier) should be hittable")
            button.tap()
            XCTAssertTrue(
                button.waitForValue("selected", timeout: 3),
                "\(operation.identifier) should become selected"
            )
        }
    }

    private func openColors() {
        if app.textFields["carrierCodeField"].exists {
            app.buttons["setupNextButton"].tap()
        }
        if app.segmentedControls["decorationPicker"].exists {
            app.buttons["setupNextButton"].tap()
        }
    }

    private func selectDecoration(_ label: String) {
        if app.textFields["carrierCodeField"].exists {
            app.buttons["setupNextButton"].tap()
        } else if app.buttons["wallpaperPaletteOption.0"].exists {
            app.buttons["setupBackButton"].tap()
        }
        let picker = app.segmentedControls["decorationPicker"]
        XCTAssertTrue(picker.waitForExistence(timeout: 3), "decoration picker should exist")

        let button = picker.buttons[label]
        XCTAssertTrue(button.waitForExistence(timeout: 3), "\(label) decoration should exist")
        scrollToHittable(button)
        XCTAssertTrue(button.isHittable, "\(label) decoration should be hittable")
        button.tap()
    }

    private func assertCoreControlsHittable(
        file: StaticString = #filePath,
        line: UInt = #line
    ) {
        let identifiers = ["backgroundColorPicker", "barColorPicker"]

        for identifier in identifiers {
            let element = control(identifier)
            XCTAssertTrue(
                element.waitForExistence(timeout: 3),
                "\(identifier) should exist",
                file: file,
                line: line
            )
            XCTAssertTrue(element.hasUsableFrame, "\(identifier) should keep a usable frame", file: file, line: line)
        }

    }

    private func control(_ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier]
    }

    private func scrollToHittable(_ element: XCUIElement) {
        for _ in 0..<4 where !element.isHittable {
            if element.frame.midY < app.frame.midY {
                app.swipeDown()
            } else {
                app.swipeUp()
            }
        }
    }
}

private enum ControlOperation {
    case palette(Int)

    var identifier: String {
        switch self {
        case .palette(let index):
            return "wallpaperPaletteOption.\(index)"
        }
    }
}

private extension XCUIElement {
    var hasUsableFrame: Bool {
        frame.width >= 40 && frame.height >= 40
    }

    func waitForValue(_ expectedValue: String, timeout: TimeInterval) -> Bool {
        let predicate = NSPredicate { element, _ in
            guard let element = element as? XCUIElement else {
                return false
            }

            return element.value as? String == expectedValue
        }
        let expectation = XCTNSPredicateExpectation(predicate: predicate, object: self)

        return XCTWaiter.wait(for: [expectation], timeout: timeout) == .completed
    }
}

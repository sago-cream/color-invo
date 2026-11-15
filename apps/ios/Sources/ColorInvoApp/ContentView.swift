import PhotosUI
import SwiftUI
import UIKit

struct ContentView: View {
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @StateObject private var model: CarrierEditorModel
    @FocusState private var carrierFieldFocused: Bool
    @State private var step: EditorStep = .carrier
    @State private var showsCustomColors = false

    private var carrierSuffixBinding: Binding<String> {
        Binding(
            get: { model.carrierSuffix },
            set: { value in
                model.updateCarrierSuffix(value)
            }
        )
    }

    init(model: CarrierEditorModel = CarrierEditorModel()) {
        _model = StateObject(wrappedValue: model)
    }

#if DEBUG
    private var opensWidgetScreenshot: Bool {
        ProcessInfo.processInfo.environment["COLORINVO_SCREENSHOT_TARGET"] == "widget"
            || ProcessInfo.processInfo.arguments.contains("--screenshot-widget")
    }
#endif

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
#if DEBUG
                if opensWidgetScreenshot {
                    widgetSection
                } else {
                    stepContent
                }
#else
                stepContent
#endif
            }
            .padding(.horizontal, 24)
            .padding(.top, 28)
            .padding(.bottom, 24)
            .frame(maxWidth: .infinity, alignment: .topLeading)
        }
        .scrollDismissesKeyboard(.interactively)
        .id(step)
        .background(ColorInvoColor.background.ignoresSafeArea())
        .safeAreaInset(edge: .bottom, spacing: 0) {
#if DEBUG
            if !opensWidgetScreenshot {
                navigationControls
            }
#else
            navigationControls
#endif
        }
        .preferredColorScheme(.light)
        .tint(ColorInvoColor.primary)
        .task {
            await model.start()
        }
        .onChange(of: step) { _, _ in
            carrierFieldFocused = false
        }
    }

    private var stepContent: some View {
        VStack(alignment: .leading, spacing: 24) {
            VStack(alignment: .leading, spacing: 12) {
                Text("步驟 \(step.rawValue + 1) / \(EditorStep.allCases.count)")
                    .colorInvoText(.secondary)
                    .accessibilityIdentifier("setupProgress")

                HStack(spacing: 8) {
                    ForEach(EditorStep.allCases) { item in
                        Capsule()
                            .fill(item.rawValue <= step.rawValue ? ColorInvoColor.primary : ColorInvoColor.hairline)
                            .frame(height: 4)
                    }
                }
                .accessibilityHidden(true)

                Text(step.title)
                    .font(.title2.bold())
                    .foregroundStyle(ColorInvoColor.text)
                    .accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("setupTitle")

                Text(step.instruction)
                    .colorInvoText(.secondary)
                    .fixedSize(horizontal: false, vertical: true)
            }

            switch step {
            case .carrier:
                carrierSection
            case .style:
                widgetSection
            case .colors:
                widgetPreview(height: 160)
                themeSection
            case .install:
                widgetPreview(height: 220)
                HStack {
                    widgetSaveStatus
                    Spacer()
                    Button("編輯載具") { step = .carrier }
                        .colorInvoText(.control)
                        .frame(minHeight: 44)
                        .accessibilityIdentifier("editCarrierButton")
                }
                VStack(alignment: .leading, spacing: 20) {
                    installInstruction(number: 1, text: "長按主畫面空白處，直到 App 開始晃動。")
                    installInstruction(number: 2, text: "點選「編輯」或「＋」，再選擇「加入小工具」。")
                    installInstruction(number: 3, text: "搜尋「條色盤」或「ColorInvo」，加入主畫面後點選「完成」。")
                }
                Text("之後可開啟 App 調整樣式，設定會自動同步至小工具。")
                    .colorInvoText(.secondary)
            }
        }
    }

    private func installInstruction(number: Int, text: LocalizedStringKey) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Text("\(number)")
                .colorInvoText(.control)
                .foregroundStyle(ColorInvoColor.primary)
                .frame(width: 28, height: 28)
                .background(ColorInvoColor.primarySoft, in: Circle())
            Text(text)
                .font(.body)
                .foregroundStyle(ColorInvoColor.text)
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    private var canContinue: Bool {
        switch step {
        case .carrier: model.isValid
        case .style: true
        case .colors: model.canCreateSettings && !model.isAnalyzingWallpaper
        case .install: true
        }
    }

    private var navigationControls: some View {
        HStack(spacing: 12) {
            if step != .carrier {
                Button("上一步") {
                    step = EditorStep(rawValue: step.rawValue - 1) ?? .carrier
                }
                .colorInvoText(.control)
                .frame(minWidth: 44, minHeight: 44)
                .accessibilityIdentifier("setupBackButton")
            }

            Button {
                step = step == .install ? .style : EditorStep(rawValue: step.rawValue + 1) ?? .carrier
            } label: {
                Text(step.nextTitle)
                    .multilineTextAlignment(.center)
                    .font(.callout.weight(.semibold))
                    .frame(maxWidth: .infinity, minHeight: 44)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .foregroundStyle(.white)
                    .background(ColorInvoColor.primary, in: RoundedRectangle(cornerRadius: 8))
            }
            .buttonStyle(.plain)
            .disabled(!canContinue)
            .colorInvoDisabledState(!canContinue)
            .accessibilityIdentifier("setupNextButton")
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 12)
        .background(ColorInvoColor.surface)
        .overlay(alignment: .top) {
            ColorInvoColor.hairline.frame(height: 1)
        }
    }

    private var carrierSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            if dynamicTypeSize.isAccessibilitySize {
                VStack(alignment: .leading, spacing: 8) {
                    Text("手機載具")
                        .colorInvoText(.heading)

                    validationBadge
                }
            } else {
                HStack(alignment: .firstTextBaseline, spacing: 12) {
                    Text("手機載具")
                        .colorInvoText(.heading)

                    Spacer()

                    validationBadge
                }
            }

            HStack(spacing: 0) {
                Text("/")
                    .colorInvoText(.code)
                    .padding(.leading, 16)

                TextField("ABC1234", text: carrierSuffixBinding)
                    .accessibilityLabel("手機載具")
                    .accessibilityIdentifier("carrierCodeField")
                    .textInputAutocapitalization(.characters)
                    .autocorrectionDisabled()
                    .keyboardType(.asciiCapable)
                    .submitLabel(.next)
                    .focused($carrierFieldFocused)
                    .colorInvoText(.code)
                    .tint(ColorInvoColor.primary)
                    .padding(.leading, 2)
                    .padding(.trailing, 16)
                    .padding(.vertical, 10)
                    .onSubmit {
                        if model.isValid {
                            carrierFieldFocused = false
                            step = .style
                        }
                    }
            }
            .background {
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .fill(ColorInvoColor.surface)
                    .overlay {
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .strokeBorder(
                                model.isValid ? ColorInvoColor.primary : ColorInvoColor.hairline,
                                lineWidth: 1
                            )
                    }
            }
        }
    }

    @ViewBuilder
    private var validationBadge: some View {
        if let validationText = model.validationText {
            HeaderStatusLabel(
                validationText,
                tone: model.isValid ? .success : .warning
            )
        }
    }

    private var themeSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            if dynamicTypeSize.isAccessibilitySize {
                VStack(alignment: .leading, spacing: 8) {
                    Text("自由條色")
                        .colorInvoText(.heading)

                    scanReadinessStatus
                }
            } else {
                HStack(alignment: .firstTextBaseline, spacing: 12) {
                    Text("自由條色")
                        .colorInvoText(.heading)

                    Spacer()

                    scanReadinessStatus
                }
            }

            wallpaperColorSection
            wallpaperPaletteChoices
            paletteFineTuningSection
            if !model.draftPalette.meetsCommercialGuidance {
                VStack(alignment: .leading, spacing: 8) {
                    Text(LocalizedStringKey(model.draftPalette.standardMessage))
                        .colorInvoText(.secondary)
                        .foregroundStyle(ColorInvoColor.warning)
                    Button("使用黑白配色") { model.useClassicPalette() }
                        .colorInvoText(.control)
                        .frame(minHeight: 44)
                        .accessibilityIdentifier("useClassicPaletteButton")
                }
            }
        }
    }

    private var wallpaperColorSection: some View {
        PhotosPicker(
            selection: $model.wallpaperPickerItem,
            matching: .images,
            photoLibrary: .shared()
        ) {
            HStack(spacing: 12) {
                if let preview = model.wallpaperPreviewImage {
                    Image(uiImage: preview.image)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 48, height: 48)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .accessibilityHidden(true)
                } else {
                    Image(systemName: "photo.on.rectangle")
                        .font(.title2)
                        .frame(width: 48, height: 48)
                }
                Text(model.wallpaperPreviewImage == nil ? "匯入桌布" : "更換桌布")
                    .colorInvoText(.control)
                Spacer()
                Image(systemName: "plus")
            }
            .foregroundStyle(ColorInvoColor.primary)
            .padding(12)
            .frame(maxWidth: .infinity, minHeight: 64)
            .background(ColorInvoColor.primarySoft, in: RoundedRectangle(cornerRadius: 20))
        }
        .buttonStyle(.plain)
        .disabled(model.isAnalyzingWallpaper)
        .colorInvoDisabledState(model.isAnalyzingWallpaper)
        .accessibilityIdentifier("importWallpaperButton")
        .onChange(of: model.wallpaperPickerItem) { _, item in
            model.loadWallpaperPalettes(from: item)
        }
    }

    private var wallpaperPaletteChoices: some View {
        VStack(alignment: .leading, spacing: 8) {
            paletteStatus
            if model.hasWallpaperColors {
                Text("桌布配色")
                    .colorInvoText(.secondary)
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 100), spacing: 8)], spacing: 8) {
                    ForEach(Array(model.wallpaperPalettes.enumerated()), id: \.offset) { index, palette in
                        let isSelected = palette.barColor == model.draftPalette.barColor
                            && palette.backgroundColor == model.draftPalette.backgroundColor
                        Button { model.selectPalette(palette) } label: {
                            VStack(spacing: 8) {
                                Code39BarcodeView(
                                    value: model.previewCode,
                                    barColor: palette.barColor.color,
                                    backgroundColor: palette.backgroundColor.color
                                )
                                .frame(height: 36)
                                .padding(8)
                                .background(palette.backgroundColor.color)
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                                .accessibilityHidden(true)
                                HStack(spacing: 4) {
                                    Text("配色 \(index + 1)")
                                        .colorInvoText(.control)
                                    Spacer(minLength: 0)
                                    Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                                        .foregroundStyle(isSelected ? ColorInvoColor.primary : ColorInvoColor.muted)
                                }
                            }
                            .padding(8)
                            .selectionCard(isSelected: isSelected, radius: 16)
                        }
                        .buttonStyle(.plain)
                        .disabled(model.isAnalyzingWallpaper)
                        .accessibilityLabel("條碼配色 \(index + 1)")
                        .accessibilityValue(isSelected ? "selected" : "not selected")
                        .accessibilityAddTraits(isSelected ? .isSelected : [])
                        .accessibilityIdentifier("wallpaperPaletteOption.\(index)")
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var paletteStatus: some View {
        if model.isAnalyzingWallpaper {
            HStack(spacing: 8) {
                ProgressView()
                Text("正在分析桌布…").colorInvoText(.secondary)
            }
            .frame(minHeight: 44)
        } else if let wallpaperStatusText = model.wallpaperStatusText {
            Text(LocalizedStringKey(wallpaperStatusText))
                .colorInvoText(.secondary)
                .frame(minHeight: 44, alignment: .leading)
        }
    }

    private var paletteFineTuningSection: some View {
        DisclosureGroup(isExpanded: $showsCustomColors) {
            VStack(spacing: 8) {
                customColorSection
                    .padding(.top, 8)
                if model.canResetWallpaperPalette {
                    Button { model.resetPaletteToWallpaper() } label: {
                        Label("還原桌布配色", systemImage: "arrow.counterclockwise")
                            .colorInvoText(.control)
                            .frame(maxWidth: .infinity, minHeight: 44)
                    }
                    .accessibilityIdentifier("resetWallpaperPaletteButton")
                }
            }
        } label: {
            Text("自訂顏色")
                .colorInvoText(.control)
                .frame(minHeight: 44)
                .accessibilityIdentifier("customColorsDisclosure")
        }
    }

    private var customColorSection: some View {
        VStack(spacing: 8) {
            backgroundColorPicker
            barColorPicker
        }
    }

    private var backgroundColorPicker: some View {
        compactColorPicker(
            title: "背景",
            color: Binding(
                get: { model.draftPalette.backgroundColor.color },
                set: { color in
                    model.updateBackgroundColor(color)
                }
            )
        )
    }

    private var barColorPicker: some View {
        compactColorPicker(
            title: "條碼",
            color: Binding(
                get: { model.draftPalette.barColor.color },
                set: { color in
                    model.updateBarColor(color)
                }
            )
        )
    }

    private func compactColorPicker(
        title: String,
        color: Binding<Color>
    ) -> some View {
        let identifier = title == "背景" ? "backgroundColorPicker" : "barColorPicker"

        return ColorPicker(selection: color, supportsOpacity: false) {
            HStack(spacing: 12) {
                Text(LocalizedStringKey(title))
                Spacer()
                Text(RGBAColor(color: color.wrappedValue).hexString)
                    .font(.system(.callout, design: .monospaced))
                    .foregroundStyle(ColorInvoColor.muted)
            }
        }
            .colorInvoText(.control)
            .padding(.horizontal, 12)
            .frame(maxWidth: .infinity)
            .frame(minHeight: 52)
            .background(ColorInvoColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            .overlay {
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .strokeBorder(ColorInvoColor.hairline, lineWidth: 1)
                    .allowsHitTesting(false)
            }
            .accessibilityIdentifier(identifier)
    }

    private var showsBarcodeValueBinding: Binding<Bool> {
        Binding(
            get: { model.showsBarcodeValue },
            set: { model.setShowsBarcodeValue($0) }
        )
    }

    private var scanReadinessStatus: some View {
        HeaderStatusLabel(
            model.draftPalette.meetsCommercialGuidance ? "適合掃描" : "對比不足",
            tone: model.draftPalette.meetsCommercialGuidance ? .success : .warning,
            accessibilityLabel: model.draftPalette.standardMessage
        )
    }

    private var widgetSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            ForEach([CarrierDecoration.cat, .wave, .none], id: \.self) { decoration in
                styleCard(decoration)
            }

            Toggle(isOn: showsBarcodeValueBinding) {
                Text("顯示載具文字")
                    .colorInvoText(.control)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .padding(.horizontal, 12)
            .frame(minHeight: 52)
            .background(ColorInvoColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            .overlay {
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .strokeBorder(ColorInvoColor.hairline, lineWidth: 1)
                    .allowsHitTesting(false)
            }
            .contentShape(Rectangle())
            .onTapGesture { model.setShowsBarcodeValue(!model.showsBarcodeValue) }
            .accessibilityIdentifier("showsBarcodeValueToggle")
        }
    }

    private func styleCard(_ decoration: CarrierDecoration) -> some View {
        let isSelected = model.decoration == decoration
        return Button { model.setDecoration(decoration) } label: {
            Group {
                if dynamicTypeSize.isAccessibilitySize {
                    VStack(alignment: .leading, spacing: 12) {
                        styleThumbnail(decoration)
                            .aspectRatio(329 / 155, contentMode: .fit)
                        styleCardLabel(decoration, isSelected: isSelected)
                    }
                } else {
                    HStack(spacing: 16) {
                        styleThumbnail(decoration)
                            .frame(width: 144, height: 68)
                        styleCardLabel(decoration, isSelected: isSelected)
                    }
                }
            }
            .padding(12)
            .frame(maxWidth: .infinity)
            .selectionCard(isSelected: isSelected, radius: 20)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(decoration.title)
        .accessibilityValue(isSelected ? "selected" : "not selected")
        .accessibilityAddTraits(isSelected ? .isSelected : [])
        .accessibilityIdentifier("decorationOption.\(decoration.rawValue)")
    }

    private func styleCardLabel(_ decoration: CarrierDecoration, isSelected: Bool) -> some View {
        HStack(spacing: 12) {
            Text(decoration.title)
                .colorInvoText(.heading)
            Spacer(minLength: 0)
            Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                .font(.title3)
                .foregroundStyle(isSelected ? ColorInvoColor.primary : ColorInvoColor.muted)
        }
    }

    private func styleThumbnail(_ decoration: CarrierDecoration) -> some View {
        scaledWidgetArtwork(decoration)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(.black.opacity(0.10), lineWidth: 1)
        }
        .accessibilityHidden(true)
    }

    private func widgetPreview(height: CGFloat) -> some View {
        scaledWidgetArtwork(model.decoration)
            .padding(16)
            .frame(height: height)
            .background { WallpaperPreviewBackground(preview: model.wallpaperPreviewImage) }
            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            .overlay {
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .strokeBorder(.black.opacity(0.10), lineWidth: 1)
            }
    }

    private func scaledWidgetArtwork(_ decoration: CarrierDecoration) -> some View {
        GeometryReader { geometry in
            let scale = min(geometry.size.width / 329, geometry.size.height / 155)
            CarrierWidgetContentView(
                carrierCode: model.previewCode,
                palette: model.draftPalette,
                dominantColors: model.wallpaperDominantColors,
                waveColor: model.selectedWaveColor,
                showsWave: decoration.showsWave,
                showsBarcodeValue: model.showsBarcodeValue,
                showsCat: decoration.showsCat,
                emptyStateText: "輸入載具即可預覽"
            )
            .frame(width: 329, height: 155)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .scaleEffect(scale, anchor: .topLeading)
            .frame(width: 329 * scale, height: 155 * scale, alignment: .topLeading)
            .position(x: geometry.size.width / 2, y: geometry.size.height / 2)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text("手機條碼 \(model.previewCode)"))
    }

    private var widgetSaveStatus: some View {
        HeaderStatusLabel(
            !model.canCreateSettings ? "尚未同步" : model.widgetIsReady ? "已同步" : "儲存中",
            tone: model.widgetIsReady ? .success : .neutral,
            systemImage: model.widgetIsReady ? nil : "arrow.triangle.2.circlepath"
        )
        .accessibilityIdentifier("widgetSaveStatus")
    }

}

private enum EditorStep: Int, CaseIterable, Identifiable {
    case carrier, style, colors, install

    var id: Self { self }

    var title: LocalizedStringKey {
        switch self {
        case .carrier: "輸入手機載具"
        case .style: "選擇小工具樣式"
        case .colors: "搭配你的桌布"
        case .install: "加入主畫面"
        }
    }

    var instruction: LocalizedStringKey {
        switch self {
        case .carrier: "輸入斜線後的 7 碼英數字或符號（+、-、.），斜線已為你填好。"
        case .style: "選擇喜歡的樣式，預覽小工具的外觀。"
        case .colors: "匯入桌布取色，或直接使用目前的配色。"
        case .install: "設定會自動儲存。確認已同步後，依下列步驟加入小工具。"
        }
    }

    var nextTitle: LocalizedStringKey {
        switch self {
        case .carrier: "下一步：選擇樣式"
        case .style: "下一步：調整配色"
        case .colors: "下一步：加入小工具"
        case .install: "調整樣式"
        }
    }
}

private extension CarrierDecoration {
    var title: LocalizedStringKey {
        switch self {
        case .cat: "貓貓"
        case .wave: "顏料"
        case .none: "極簡"
        }
    }
}

private extension RGBAColor {
    var hexString: String {
        String(format: "#%02X%02X%02X", Int((red * 255).rounded()), Int((green * 255).rounded()), Int((blue * 255).rounded()))
    }
}

private enum HeaderStatusTone {
    case success
    case warning
    case neutral

    var color: Color {
        switch self {
        case .success:
            ColorInvoColor.success
        case .warning:
            ColorInvoColor.warning
        case .neutral:
            ColorInvoColor.muted
        }
    }

    var systemImage: String {
        switch self {
        case .success:
            "checkmark.circle.fill"
        case .warning:
            "exclamationmark.triangle.fill"
        case .neutral:
            "clock"
        }
    }
}

private struct HeaderStatusLabel: View {
    let text: String
    let tone: HeaderStatusTone
    let systemImage: String
    let accessibilityText: String

    init(
        _ text: String,
        tone: HeaderStatusTone,
        systemImage: String? = nil,
        accessibilityLabel: String? = nil
    ) {
        self.text = text
        self.tone = tone
        self.systemImage = systemImage ?? tone.systemImage
        accessibilityText = accessibilityLabel ?? text
    }

    var body: some View {
        Label {
            Text(LocalizedStringKey(text))
                .colorInvoText(.secondary)
                .foregroundStyle(tone.color)
                .fixedSize(horizontal: false, vertical: true)
        } icon: {
            Image(systemName: systemImage)
                .font(.callout)
                .foregroundStyle(tone.color)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text(LocalizedStringKey(accessibilityText)))
    }
}

private struct WallpaperPreviewBackground: View {
    let preview: WallpaperPreviewImage?

    var body: some View {
        ZStack {
            if let preview {
                Image(uiImage: preview.image)
                    .resizable()
                    .scaledToFill()

                Color.white.opacity(0.52)
            } else {
                ColorInvoColor.primarySoft
            }
        }
        .clipped()
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}

private enum ColorInvoTextStyle {
    case heading
    case control
    case secondary
    case code
}

private struct ColorInvoTextStyleModifier: ViewModifier {
    let style: ColorInvoTextStyle

    func body(content: Content) -> some View {
        switch style {
        case .heading:
            content
                .font(.headline)
                .foregroundStyle(ColorInvoColor.text)
        case .control:
            content
                .font(.callout.weight(.semibold))
                .foregroundStyle(ColorInvoColor.text)
        case .secondary:
            content
                .font(.callout.weight(.semibold))
                .foregroundStyle(ColorInvoColor.muted)
        case .code:
            content
                .font(.system(.body, design: .monospaced, weight: .semibold))
                .foregroundStyle(ColorInvoColor.text)
        }
    }
}

private extension View {
    nonisolated func colorInvoText(_ style: ColorInvoTextStyle) -> some View {
        modifier(ColorInvoTextStyleModifier(style: style))
    }

    nonisolated func selectionCard(isSelected: Bool, radius: CGFloat) -> some View {
        background(isSelected ? ColorInvoColor.primarySoft : ColorInvoColor.surface, in: RoundedRectangle(cornerRadius: radius))
            .overlay {
                RoundedRectangle(cornerRadius: radius)
                    .strokeBorder(isSelected ? ColorInvoColor.primary : ColorInvoColor.hairline, lineWidth: isSelected ? 2 : 1)
            }
            .contentShape(RoundedRectangle(cornerRadius: radius))
    }

    nonisolated func colorInvoDisabledState(_ isDisabled: Bool) -> some View {
        opacity(isDisabled ? ColorInvoControlState.disabledOpacity : 1)
    }
}

private enum ColorInvoControlState {
    static let disabledOpacity = 0.44
}

private enum ColorInvoColor {
    static let frozenLake = Color(red: 112 / 255, green: 214 / 255, blue: 255 / 255)
    static let roseKiss = Color(red: 255 / 255, green: 112 / 255, blue: 166 / 255)
    static let primary = Color(red: 0 / 255, green: 126 / 255, blue: 168 / 255)
    static let attention = Color(red: 180 / 255, green: 98 / 255, blue: 20 / 255)
    static let background = Color.white
    static let surface = Color.white
    static let primarySoft = Color(red: 234 / 255, green: 248 / 255, blue: 255 / 255)
    static let hairline = Color(red: 216 / 255, green: 230 / 255, blue: 238 / 255)
    static let text = Color(red: 17 / 255, green: 24 / 255, blue: 39 / 255)
    static let muted = Color(red: 82 / 255, green: 96 / 255, blue: 106 / 255)
    static let success = primary
    static let warning = attention
}

#Preview {
    ContentView()
}

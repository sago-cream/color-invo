# App Store Screenshot Design QA

- Source visual truth: the three tracked outputs under `assets/screenshots`, revised by the latest user brief so both remaining copy groups occupy approximately 85% of their usable card width.
- Implementation screenshots: `assets/screenshots/colorinvo-iphone-6-5-01-wallpaper-palette.png`, `assets/screenshots/colorinvo-iphone-6-5-02-decorations.png`, and `assets/screenshots/colorinvo-iphone-6-5-03-scanner-widget.png`.
- Viewport: three adjacent 1284 × 2778 App Store screenshots; combined panorama 3852 × 2778.
- State: Traditional Chinese showcase data, light appearance, orange paint widget with `#222222` bars and blue cat widget.
- Full-view comparison evidence: the three tracked output panels form the complete 3852 × 2778 panorama.
- Focused comparison evidence: the individual tracked panels verify the isolated-card crops, enlarged typography, opposing alignment, and the text-free center bridge.

## Findings

- No actionable P0, P1, or P2 differences remain.
- Fonts and typography: each line is fitted independently to 85% of the 1112 px usable card width. Both headlines render at 163 px black; the top-right supporting line renders at 75 px bold and the bottom-left supporting line at 65 px bold. Color emphasis remains limited to 桌布配色、不再破壞桌布氛圍、額外裝飾 and 安全可掃. All copy remains on one line without truncation.
- Spacing and layout rhythm: the orange paint widget begins 96 px from the panorama's top-left and the blue cat widget ends 96 px from the bottom-right. Both retain their native 329:155 ratio and stay centered on their seams. 提取桌布配色 is right-aligned in the panorama's top-right corner; 選擇額外裝飾 is left-aligned in the bottom-left corner. The opposite anchors balance the strip while the center card becomes an uninterrupted visual bridge.
- Colors and visual tokens: the paint area remains app-icon orange `#FF9770`, its scanner-safe background remains peach `#FFE4D9`, and its barcode bars are now neutral black `#222222`. The blue widget retains its tint derived from app-icon blue `#70D6FF`. Under the app's red-light reflectance model, the blue pair has background/bar reflectance 0.749/0.000 and symbol contrast 0.749; the orange widget pair has 1.000/0.133 and symbol contrast 0.867. Both exceed the 0.70 commercial guidance threshold.
- Image quality and asset fidelity: both elements are real widget captures, not reconstructed illustrations. Their native masks, barcode geometry, cat damage treatment, paint shape, and shadows remain sharp at 1284 × 2778 output.
- Copy and content: the 好看，也好掃 group is removed. Only 提取桌布配色 and 選擇額外裝飾 remain, with their existing supporting lines and requested color emphasis.

## Comparison History

1. The previous two-widget version used one blue palette, placed the cat upper-left and paint lower-right, and aligned all copy at the top.
2. The revised implementation swaps the widgets, introduces brand-derived scan-safe orange and blue states, centers each widget on its seam, and redistributes the copy to use the negative space.
3. The latest refinement replaced the orange paint state with dark yellow, changed 裝飾 to 額外裝飾, and removed emphasis from 小工具 and 貓貓 in favor of the requested benefit phrases.
4. The correction restored the orange paint and peach background, limited dark yellow to the barcode bars, and added blue emphasis to 不再破壞桌布氛圍.
5. The final color refinement changed those bars to `#222222` and moved the card 2 copy group 60 px closer to the cat widget.
6. The two-message refinement removed 好看，也好掃, enlarged the remaining copy, and anchored the two groups at opposite outer corners.
7. The typography refinement fits every remaining title and supporting line to approximately 85% of its usable card width.
8. The full panorama and isolated-card comparison found no remaining P0, P1, or P2 issues.

## Open Questions

- None.

## Implementation Checklist

- [x] Swap paint and cat widget positions.
- [x] Center both widget frames on their respective seams.
- [x] Preserve the native widget aspect ratio and shared 96 px outer inset.
- [x] Keep the orange paint and peach background while changing only its barcode bars to scan-safe `#222222`.
- [x] Remove the third copy group.
- [x] Enlarge the two remaining copy groups.
- [x] Fit every copy line to approximately 85% of the usable card width.
- [x] Right-align 提取桌布配色 at the top-right and left-align 選擇額外裝飾 at the bottom-left.
- [x] Verify all three 1284 × 2778 outputs and standalone crops.

## Follow-up Polish

- None required for this pass.

final result: passed

---

# Landing Page Design QA

- Source visual truth path:
  `.codex-screenshots/ontrack-landing-reference.png`
- Implementation screenshot paths:
  `.codex-screenshots/colorinvo-landing-desktop.png` and
  `.codex-screenshots/colorinvo-landing-mobile.png`
- Combined comparison:
  `.codex-screenshots/colorinvo-landing-comparison.png`
- Desktop viewport: 960 × 900 CSS px at device scale 1. Browser scrollbar
  normalization produced 959 px and 945 px wide full-page captures for the
  source and implementation.
- Mobile viewport: 390 × 844 CSS px at device scale 1. The in-app browser
  produced a 375 × 2314 px content capture after browser chrome normalization.
- State: Traditional Chinese, light appearance, cat widget selected.
- Primary interactions tested: Cat/Paint/Minimal selector, official App Store
  link target, `/legal`, localized footer navigation, and responsive layout.
- Browser console: no errors or warnings in the final desktop implementation
  capture.

## Full-view comparison evidence

The combined comparison verifies the requested structural reference rather than
a literal visual clone. Both pages use a restrained hero followed by a central
product capture with anchored explanatory cards. ColorInvo intentionally keeps
its existing typography, cyan palette, icon, footer, and legal-page system while
adapting OnTrack's product-story rhythm to the three-step carrier setup.

## Focused comparison evidence

The mobile capture verifies the areas that are too small to judge in the
combined desktop view: the hero headline wraps cleanly in two lines, the
official App Store badge remains legible, the complete widget frame is visible
without clipping, the selector retains 44 px targets, and the three annotated
steps collapse into the intended reading order below the editor image.

## Findings

- No actionable P0, P1, or P2 differences remain.
- Fonts and typography: the existing Iowan/Palatino display family and IBM
  Plex/Avenir body stack preserve ColorInvo's identity. Desktop hierarchy,
  mobile wrapping, weights, and line heights remain readable without
  truncation.
- Spacing and layout rhythm: the desktop feature cards retain OnTrack's
  anchored-callout pattern while the mobile layout removes connectors and
  becomes a linear image-first story. Spacing, radii, and shadows use the
  project's existing 4 px token scale.
- Colors and visual tokens: all new surfaces, borders, controls, icons, and
  shadows use ColorInvo tokens. Contrast remains clear in the selected and
  unselected widget-style states.
- Image quality and asset fidelity: the showcase uses full 1140 × 654 crops
  from real simulator captures, and the central product image uses the latest
  1284 × 2778 simulator capture with the cat-style picker. The colorful widget
  boundary, barcode, and cat are all visible; no product artwork is
  reconstructed with CSS.
- Copy and content: the approved accessory-led hero copy, three-step flow,
  on-device trust note, localized App Store CTA, and legal/logo-use guidance are
  present in Traditional Chinese and English.
- Accessibility and interaction: the selector exposes pressed state and live
  image alt text, focus states are inherited from the site system, reduced
  motion is respected, and navigation remains semantic.

## Comparison History

1. Initial implementation used the inner 1044 × 492 barcode crop.
2. The user requested an uncropped widget preview, App Store CTA, and legal
   logo-use page.
3. The showcase was replaced with full 1140 × 654 widget captures, the official
   localized badges and listing URL were added, and `/legal` plus `/en/legal`
   were implemented.
4. The first mobile review found an awkward split inside `完美配色`; reducing the
   mobile hero size produced a balanced two-line headline.
5. Final desktop and mobile captures found no remaining P0, P1, or P2 issues.
6. The central editor image was updated from the July 3 website capture to the
   latest product capture used by the current release workflow.
7. The desktop callouts were realigned to the new screenshot: decoration beside
   the style picker, scan-safe generation beside the widget preview, and
   wallpaper colors beside the import and palette controls. Mobile keeps the
   original three-step reading order.
8. The header now follows the OnTrack reference with branding on the left and a
   plain-text App Store CTA on the right. Widget styles move through one
   horizontal, interruptible track, and the redundant device-storage note was
   removed.
9. The landing-page system now uses the app icon's `#FF9770` orange as its
   primary color, supported by warm neutral surfaces and a darker accessible
   orange for text and focus states. The top-right CTA is an orange “App Store”
   button.
10. The App Store artwork composer uses `NSFont.systemFont` with black title
    weight and bold supporting headings. Landing-page headings now use the
    equivalent San Francisco system stack with PingFang TC for Traditional
    Chinese, replacing the unrelated Iowan serif display face.
11. The central product screenshot was recolored to the same orange system as
    the landing page. Its widget uses a `#FF9770`-led peach preview, pale inner
    surface, and deep burnt-orange barcode and cat treatment; blue UI accents
    were likewise replaced with coordinated orange states.
12. The 375 × 667 annotation pass hides the redundant header CTA below 480 px,
    centers the official badge when the hero becomes single-column at 960 px,
    and updates the Traditional Chinese title to
    「為你的載具挑選完美配色（或是貓貓）」. The localized phrase 「完美配色」
    is kept together at every width.
13. The orange landing-page experiment was rolled back in favor of the iOS
    app's actual `ColorInvoTheme.primary` value, `#007EA8`. The original cool
    surfaces, blue interaction states, blue widget showcase captures, and
    original blue product screenshot are again used consistently.
14. Headline wrapping now follows explicit semantic boundaries instead of
    character-level opportunities. Traditional Chinese copy also uses the
    browser's phrase-aware line-breaking mode, preventing breaks inside natural
    phrases throughout the site.
15. The hero is now a centered vertical product story, with the widget selector
    preceding the uncropped widget. The three-step heading uses the smaller
    heading scale, its callout cards are icon-free and shadow-free, and their
    copy is shorter in both locales. The footer now mirrors OnTrack's copyright
    treatment with `© 2026 ColorInvo`.
16. The website typography was reduced from four semantic font aliases, six
    base constructions, and ten rendered tuples to three aliases, three
    constructions, and five tuples. All web text now uses the system stack;
    body size, leading, link weight, and responsive display behavior are
    consolidated without changing the iOS app.

## Open Questions

- None.

## Implementation Checklist

- [x] Preserve the complete widget frame at every breakpoint.
- [x] Make Cat, Paint, and Minimal preview states interactive.
- [x] Add localized official App Store badges and the live ColorInvo listing.
- [x] Adapt OnTrack's anchored feature analysis to the three-step setup flow.
- [x] Add localized legal pages and footer links for logo usage.
- [x] Verify desktop, mobile, selector state, legal content, and browser output.

## Follow-up Polish

- None required for this pass.

final result: passed

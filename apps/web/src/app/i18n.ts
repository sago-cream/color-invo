import type { Metadata } from 'next';

import type { Locale, SitePage } from './site';
import { absoluteRoute, routeFor, SITE } from './site';

export const COPY = {
    en: {
        brand: SITE.name,
        demoAlt:
            'ColorInvo app screen showing carrier setup and widget preview',
        demoLabel: 'ColorInvo app screen demo',
        htmlLang: 'en',
        lastUpdated: 'September 12, 2026',
        metadata: {
            home: {
                description:
                    'Match your Taiwan mobile invoice carrier widget to your wallpaper, add a cat or another decoration, and keep it ready on your iPhone Home Screen.',
                title: 'ColorInvo - Make your carrier part of your Home Screen',
            },
            legal: {
                description:
                    'ColorInvo legal notices and guidelines for using the ColorInvo name, app icon, and logo.',
                title: 'Legal Notice',
            },
            privacy: {
                description:
                    'ColorInvo privacy policy for local carrier settings, wallpaper color generation, widgets, and support contact.',
                title: 'Privacy Policy',
            },
            support: {
                description:
                    'Get help with ColorInvo carrier setup, widgets, color choices, and bug reports.',
                title: 'Support',
            },
        },
        ogLocale: 'en_US',
        pages: {
            home: {
                appStoreBadge: {
                    alt: 'Download ColorInvo on the App Store',
                    src: '/app-store-badge-en.svg',
                },
                flowTitle: 'Three steps to make your carrier belong.',
                lede: 'Extract colors from your wallpaper, add the decoration you like, and keep your carrier ready at hand as the perfect Home Screen accessory.',
                showcase: {
                    label: 'Choose a ColorInvo widget style',
                    options: [
                        {
                            alt: 'Cat decoration playfully woven into a blue carrier barcode widget',
                            label: 'Cat',
                            src: '/colorinvo-widget-cat.png',
                        },
                        {
                            alt: 'Paint decoration flowing across a blue carrier barcode widget',
                            label: 'Paint',
                            src: '/colorinvo-widget-wave.png',
                        },
                        {
                            alt: 'Minimal blue carrier barcode widget',
                            label: 'Minimal',
                            src: '/colorinvo-widget-plain.png',
                        },
                    ],
                },
                steps: [
                    {
                        body: 'Pull colors from your wallpaper for a barcode that fits your Home Screen.',
                        title: 'Match your wallpaper',
                    },
                    {
                        body: 'Add Cat, Paint, or Minimal to make it yours.',
                        title: 'Choose a decoration',
                    },
                    {
                        body: 'Every palette is contrast-checked to keep the barcode easy to scan.',
                        title: 'Keep it easy to scan',
                    },
                ],
                title: 'Pick the perfect colors for your carrier—or add a cat.',
                titlePhrases: [
                    'Pick the perfect colors ',
                    'for your carrier',
                    '—or add a cat.',
                ],
            },
            legal: {
                contactBody:
                    'For permission to use ColorInvo brand assets beyond these guidelines, contact',
                contactTitle: 'Permission requests',
                sections: [
                    {
                        body: [
                            'ColorInvo, 條色盤, the ColorInvo app icon, and related logos are identifying assets of Hsi Chen. Do not use them in a way that suggests an official partnership, endorsement, or affiliation without prior written permission.',
                            'Editorial coverage, reviews, and commentary may use the ColorInvo name and an unmodified app icon in reasonable context. Do not redraw, recolor, add effects to, or use these assets as the identity of another product, service, website, or social account.',
                        ],
                        title: 'Name, icon, and logo',
                    },
                    {
                        body: [
                            'Apple, App Store, iPhone, and iOS are trademarks of Apple Inc., registered in the U.S. and other countries and regions. The App Store badge is used in accordance with Apple marketing guidelines.',
                        ],
                        title: 'Third-party marks',
                    },
                ],
                title: 'Legal Notice',
            },
            privacy: {
                sections: [
                    {
                        body: [
                            'ColorInvo stores your Taiwan mobile invoice carrier, barcode colors, and widget settings locally on your device. The app does not sell personal data, use third-party advertising, or require a ColorInvo account.',
                        ],
                        title: 'Summary',
                    },
                    {
                        items: [
                            'Carrier and color settings are stored in an iOS app group or private Android app storage so the app and widget can read the same saved setup.',
                            'Wallpaper colors are generated on device from the image you select. ColorInvo saves the selected colors and a small local preview for the Home Screen preview.',
                            'If you email support, your email address and message content are used to respond to that request.',
                        ],
                        title: 'Stored information',
                    },
                    {
                        body: [
                            'ColorInvo only reads the image you choose through the system photo picker. The app does not browse your full photo library and does not upload selected images to a ColorInvo service. The preview image stays on your device.',
                        ],
                        title: 'Photos',
                    },
                    {
                        body: [
                            'The widget reads saved settings from local app storage on the same device. ColorInvo does not share those settings with an external ColorInvo server.',
                            'Apple and Google may process store downloads, purchases, diagnostics, crash reports, or testing feedback under their own policies and your settings. Android may back up settings and the wallpaper preview or transfer them to a new device according to system settings.',
                        ],
                        title: 'Sharing',
                    },
                    {
                        body: [
                            'You can change saved settings in the app. To delete local data, uninstall ColorInvo or use Clear storage in Android app settings. Manage system backups separately; a backup may restore data after reinstalling. Support emails are retained as needed to respond to and track the request.',
                        ],
                        title: 'Retention and control',
                    },
                    {
                        body: [
                            `This website is hosted at ${SITE.domain}. Hosting providers may process basic request information such as IP address, user agent, and timestamps to operate and secure the site.`,
                        ],
                        title: 'Website',
                    },
                ],
                contactBody: 'Questions about this policy can be sent to',
                contactTitle: 'Contact',
                lastUpdatedLabel: 'Last updated:',
                title: 'Privacy Policy',
            },
            support: {
                commonIssuesTitle: 'Common issues',
                contactBody:
                    'Need help? Email us. When reporting a bug, include the details below so the issue can be reproduced.',
                contactTitle: 'Contact',
                issues: [
                    {
                        body: 'Reopen ColorInvo, confirm that the carrier format is valid and colors are scanner-ready, then wait for the synced indicator and the system to refresh the widget.',
                        icon: 'phone',
                        title: 'Widget does not show the barcode',
                    },
                    {
                        body: 'Choose a still image from Photos. The image is only used for local color generation and preview, and is not uploaded to ColorInvo.',
                        icon: 'sparkles',
                        title: 'Wallpaper colors cannot be generated',
                    },
                    {
                        body: 'Use a higher-contrast color choice and avoid red bars. Store scanners commonly use red light, so red bars can reduce readability.',
                        icon: 'bug',
                        title: 'Barcode scan fails at checkout',
                    },
                ],
                reportIntro: 'Please include:',
                reportItems: [
                    'Device model and iOS or Android version.',
                    'ColorInvo version from the app store or testing build.',
                    'A screenshot or screen recording when it helps explain the issue.',
                    'Steps to reproduce, including whether the issue is in the app or widget.',
                ],
                reportTitle: 'Bug reports',
                responseTime:
                    'Support is handled by the app developer. Most messages receive a reply within 1-3 business days.',
                setupItems: [
                    'Open ColorInvo and enter the seven characters after the leading slash (eight characters total).',
                    'Choose barcode colors, or generate colors from a wallpaper photo on device.',
                    'Wait for settings to sync automatically, then add ColorInvo from the Home Screen widget picker on iOS or Android.',
                ],
                setupTitle: 'Setup checklist',
                supportUrlLabel: 'Support URL:',
                title: 'Support',
            },
        },
        shell: {
            appStoreCta: 'App Store',
            footerBrand: `© 2026 ${SITE.name}`,
            footerLinks: {
                legal: 'Legal',
                privacy: 'Privacy',
                support: 'Support',
            },
            navLabel: 'Primary',
        },
    },
    zh: {
        brand: SITE.localName,
        demoAlt: '條色盤畫面顯示手機條碼設定與小工具預覽',
        demoLabel: '條色盤畫面示意',
        htmlLang: 'zh-Hant-TW',
        lastUpdated: '2026 年 9 月 12 日',
        metadata: {
            home: {
                description:
                    '從桌布提取配色，搭配貓貓等裝飾，讓手機載具成為 iPhone 主畫面的完美配件。',
                title: '條色盤 - 讓手機載具成為桌面的完美配件',
            },
            legal: {
                description:
                    '條色盤法律聲明與名稱、應用程式圖示及標誌使用規範。',
                title: '法律聲明',
            },
            privacy: {
                description:
                    '條色盤隱私權政策：本機手機條碼設定、桌布取色、小工具與支援聯絡。',
                title: '隱私權政策',
            },
            support: {
                description:
                    '條色盤支援資訊：手機條碼設定、小工具、配色與錯誤回報。',
                title: '支援',
            },
        },
        ogLocale: 'zh_TW',
        pages: {
            home: {
                appStoreBadge: {
                    alt: '在 App Store 下載條色盤',
                    src: '/app-store-badge-zh.svg',
                },
                flowTitle: '三步，讓載具融入桌面。',
                lede: '從桌布提取配色，搭配喜歡的裝飾，讓載具隨手可用，也成為手機桌面的完美配件。',
                showcase: {
                    label: '選擇條色盤小工具樣式',
                    options: [
                        {
                            alt: '貓貓藏在藍色載具條碼中的小工具',
                            label: '貓貓',
                            src: '/colorinvo-widget-cat.png',
                        },
                        {
                            alt: '顏料流過藍色載具條碼的小工具',
                            label: '顏料',
                            src: '/colorinvo-widget-wave.png',
                        },
                        {
                            alt: '藍色極簡載具條碼小工具',
                            label: '極簡',
                            src: '/colorinvo-widget-plain.png',
                        },
                    ],
                },
                steps: [
                    {
                        body: '從桌布取色，快速產生與主畫面協調的條碼配色。',
                        title: '提取桌布配色',
                    },
                    {
                        body: '加上貓貓、顏料或極簡裝飾，搭出你的風格。',
                        title: '加上喜歡的裝飾',
                    },
                    {
                        body: '每組配色都會檢查對比，確保條碼清楚可掃。',
                        title: '確保條碼清楚可掃',
                    },
                ],
                title: '為你的載具挑選完美配色（或是貓貓）',
                titlePhrases: ['為你的載具', '挑選完美配色', '（或是貓貓）'],
            },
            legal: {
                contactBody: '若要在上述範圍以外使用條色盤品牌素材，請先聯絡',
                contactTitle: '使用許可',
                sections: [
                    {
                        body: [
                            '「條色盤」、ColorInvo、條色盤應用程式圖示與相關標誌是 Hsi Chen 的識別資產。除非事先取得書面同意，不得以暗示官方合作、認可或隸屬關係的方式使用。',
                            '新聞報導、評論或介紹可在合理範圍內使用條色盤名稱與未經修改的應用程式圖示。不得重繪、重新配色、添加效果，或將這些素材用作其他產品、服務、網站或社群帳號的識別。',
                        ],
                        title: '名稱、圖示與標誌',
                    },
                    {
                        body: [
                            'Apple、App Store、iPhone 與 iOS 是 Apple Inc. 在美國及其他國家或地區註冊的商標。App Store 徽章依 Apple 行銷規範使用。',
                        ],
                        title: '第三方商標',
                    },
                ],
                title: '法律聲明',
            },
            privacy: {
                sections: [
                    {
                        body: [
                            '條色盤會把台灣手機條碼、條碼配色與小工具設定儲存在你的裝置上。應用程式不販售個人資料、不使用第三方廣告，也不需要條色盤帳號。',
                        ],
                        title: '摘要',
                    },
                    {
                        items: [
                            '手機條碼與配色設定會儲存在 iOS 應用程式群組或 Android 應用程式的私有空間，讓應用程式與小工具讀取同一份設定。',
                            '桌布配色由你選擇的圖片在裝置上產生。條色盤會儲存選定的配色，以及用於主畫面預覽的小型本機預覽圖。',
                            '如果你寄信聯絡支援，寄件地址與信件內容會用於回覆該次請求。',
                        ],
                        title: '儲存的資訊',
                    },
                    {
                        body: [
                            '條色盤只會透過系統照片選擇器讀取你選擇的圖片。應用程式不會瀏覽完整照片圖庫，也不會把選取圖片上傳到條色盤服務。預覽圖會留在你的裝置上。',
                        ],
                        title: '照片',
                    },
                    {
                        body: [
                            '小工具會從同一台裝置的本機應用程式儲存空間讀取已儲存設定。條色盤不會把這些設定分享給外部伺服器。',
                            'Apple 與 Google 可能依照各自政策與使用者設定處理商店下載、購買、診斷資料、當機報告或測試回饋。Android 可能依系統設定備份設定與桌布預覽圖，或移轉至新裝置。',
                        ],
                        title: '分享',
                    },
                    {
                        body: [
                            '你可以在應用程式中變更設定。若要刪除本機資料，請解除安裝條色盤，或在 Android 應用程式設定中清除儲存空間。系統備份需另行管理；重新安裝後可能從備份還原資料。支援信件會視回覆與追蹤請求所需保留。',
                        ],
                        title: '保留與控制',
                    },
                    {
                        body: [
                            `本網站託管於 ${SITE.domain}。網站代管服務可能為了營運與保護網站，處理基本請求資訊，例如 IP 位址、使用者代理與時間戳記。`,
                        ],
                        title: '網站',
                    },
                ],
                contactBody: '對此政策有疑問，請寄信至',
                contactTitle: '聯絡方式',
                lastUpdatedLabel: '最後更新：',
                title: '隱私權政策',
            },
            support: {
                commonIssuesTitle: '常見問題',
                contactBody:
                    '需要協助請寄信給我們。回報問題時請附上下面資訊，方便確認與重現。',
                contactTitle: '聯絡方式',
                issues: [
                    {
                        body: '重新開啟條色盤，確認手機條碼格式正確且配色適合掃描，等候顯示已同步，再稍等系統更新小工具內容。',
                        icon: 'phone',
                        title: '小工具沒有顯示條碼',
                    },
                    {
                        body: '請從照片選擇靜態圖片。圖片只用於本機取色與預覽，不會上傳到條色盤服務。',
                        icon: 'sparkles',
                        title: '無法從桌布產生配色',
                    },
                    {
                        body: '請改用對比更高的配色，並避免紅色條碼。商店掃描器常用紅光，紅色條碼會降低可讀性。',
                        icon: 'bug',
                        title: '結帳掃描失敗',
                    },
                ],
                reportIntro: '請包含：',
                reportItems: [
                    '裝置型號與系統版本。',
                    '商店或測試版本頁面上顯示的條色盤版本。',
                    '能說明問題的截圖或錄影。',
                    '重現步驟，並註明問題發生在應用程式或小工具。',
                ],
                reportTitle: '錯誤回報',
                responseTime:
                    '支援由應用程式開發者處理，多數訊息會在 1-3 個工作天內回覆。',
                setupItems: [
                    '開啟條色盤，輸入斜線後方七碼手機條碼載具（含斜線共八碼）。',
                    '選擇條碼配色，或從桌布照片在裝置上產生配色。',
                    '等候設定自動同步後，到 iOS 或 Android 主畫面的小工具選單加入條色盤。',
                ],
                setupTitle: '設定檢查',
                supportUrlLabel: '支援網址：',
                title: '支援',
            },
        },
        shell: {
            appStoreCta: 'App Store',
            footerBrand: `© 2026 ${SITE.name}`,
            footerLinks: {
                legal: '法律聲明',
                privacy: '隱私權',
                support: '支援',
            },
            navLabel: '主要導覽',
        },
    },
} as const;

export function getCopy(locale: Locale): (typeof COPY)[Locale] {
    return COPY[locale];
}

export function pageMetadata(locale: Locale, page: SitePage): Metadata {
    const copy = getCopy(locale);
    const pageCopy = copy.metadata[page];
    const otherLocale: Locale = locale === 'zh' ? 'en' : 'zh';

    return {
        alternates: {
            canonical: routeFor(locale, page),
            languages: {
                'en': routeFor('en', page),
                'x-default': routeFor('zh', page),
                'zh-Hant-TW': routeFor('zh', page),
            },
        },
        description: pageCopy.description,
        openGraph: {
            alternateLocale: [getCopy(otherLocale).ogLocale],
            description: pageCopy.description,
            images: [
                {
                    alt: copy.pages.home.showcase.options[0].alt,
                    height: 654,
                    url: '/colorinvo-widget-cat.png',
                    width: 1140,
                },
            ],
            locale: copy.ogLocale,
            siteName: copy.brand,
            title: pageCopy.title,
            url: absoluteRoute(locale, page),
        },
        title: pageCopy.title,
    };
}

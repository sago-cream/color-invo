export const SITE = {
    appStoreUrl:
        'https://apps.apple.com/tw/app/%E6%A2%9D%E8%89%B2%E7%9B%A4/id6786967206',
    name: 'ColorInvo',
    localName: '條色盤',
    url: 'https://colorinvo.hsichen.dev',
    domain: 'colorinvo.hsichen.dev',
    supportEmail: 'its.hsichen@gmail.com',
} as const;

export const ROUTES = {
    en: {
        home: '/en',
        legal: '/en/legal',
        privacy: '/en/privacy',
        support: '/en/support',
    },
    zh: {
        home: '/',
        legal: '/legal',
        privacy: '/privacy',
        support: '/support',
    },
} as const;

export type Locale = keyof typeof ROUTES;
export type SitePage = keyof (typeof ROUTES)['zh'];

export const DEFAULT_LOCALE: Locale = 'zh';

export const LOCALES = ['zh', 'en'] as const satisfies readonly Locale[];

export function routeFor(locale: Locale, page: SitePage): string {
    return ROUTES[locale][page];
}

export function absoluteRoute(locale: Locale, page: SitePage): string {
    return `${SITE.url}${routeFor(locale, page)}`;
}

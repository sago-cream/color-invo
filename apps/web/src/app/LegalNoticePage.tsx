import type { JSX } from 'react';

import { getCopy } from './i18n';
import { LegalPage } from './LegalPage';
import type { Locale } from './site';
import { SITE } from './site';

interface LegalNoticePageContentProps {
    readonly locale: Locale;
}

export function LegalNoticePageContent({
    locale,
}: LegalNoticePageContentProps): JSX.Element {
    const page = getCopy(locale).pages.legal;

    return (
        <LegalPage currentPage='legal' locale={locale} title={page.title}>
            {page.sections.map((section) => (
                <section className='legalSection' key={section.title}>
                    <h2 className='legalSection__title'>{section.title}</h2>
                    {section.body.map((paragraph) => (
                        <p key={paragraph}>{paragraph}</p>
                    ))}
                </section>
            ))}
            <section className='legalSection'>
                <h2 className='legalSection__title'>{page.contactTitle}</h2>
                <p>
                    {page.contactBody}{' '}
                    <a
                        className='legalLink'
                        href={`mailto:${SITE.supportEmail}?subject=ColorInvo%20Brand%20Usage`}
                    >
                        {SITE.supportEmail}
                    </a>
                    .
                </p>
            </section>
        </LegalPage>
    );
}

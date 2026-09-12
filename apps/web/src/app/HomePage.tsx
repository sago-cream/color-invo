import type { JSX } from 'react';
import Image from 'next/image';

import { getCopy } from './i18n';
import { SiteShell } from './LegalPage';
import type { Locale } from './site';
import { SITE } from './site';
import { WidgetShowcase } from './WidgetShowcase';

interface HomePageContentProps {
    readonly locale: Locale;
}

export function HomePageContent({ locale }: HomePageContentProps): JSX.Element {
    const copy = getCopy(locale);
    const page = copy.pages.home;
    const stepPresentation = ['bottomRight', 'topRight', 'middleLeft'] as const;

    return (
        <SiteShell currentPage='home' locale={locale}>
            <section className='homeHero'>
                <div className='homeHero__copy'>
                    <h1 className='homeHero__title'>
                        {page.titlePhrases.map((phrase, index) => (
                            <span key={phrase}>
                                {index > 0 && <wbr />}
                                <span className='homeHero__titlePhrase'>
                                    {phrase}
                                </span>
                            </span>
                        ))}
                    </h1>
                    <p className='homeHero__lede'>{page.lede}</p>
                    <a
                        aria-label={page.appStoreBadge.alt}
                        className='appStoreCta'
                        href={SITE.appStoreUrl}
                    >
                        <Image
                            alt={page.appStoreBadge.alt}
                            className='appStoreCta__badge'
                            height={83}
                            src={page.appStoreBadge.src}
                            width={250}
                        />
                    </a>
                </div>
                <WidgetShowcase
                    label={page.showcase.label}
                    options={page.showcase.options}
                />
            </section>
            <section
                aria-labelledby='home-flow-title'
                className='homeSection homeFlow'
            >
                <div className='homeFlow__intro'>
                    <h2 id='home-flow-title'>{page.flowTitle}</h2>
                </div>
                <div className='homeFlow__analysis'>
                    {page.steps.map((step, index) => {
                        const placement = stepPresentation[index];

                        return (
                            <article
                                className='homeFlow__step'
                                data-placement={placement}
                                key={step.title}
                            >
                                <div className='homeFlow__content'>
                                    <h3>{step.title}</h3>
                                    <p>{step.body}</p>
                                </div>
                            </article>
                        );
                    })}
                    <div
                        aria-label={copy.demoLabel}
                        className='homeFlow__media'
                    >
                        <Image
                            alt={copy.demoAlt}
                            className='homeFlow__image'
                            height={2778}
                            loading='eager'
                            src='/colorinvo-demo.png'
                            width={1284}
                        />
                    </div>
                </div>
            </section>
        </SiteShell>
    );
}

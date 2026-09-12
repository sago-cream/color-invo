import type { JSX } from 'react';
import type { Metadata } from 'next';

import { pageMetadata } from '../../i18n';
import { LegalNoticePageContent } from '../../LegalNoticePage';

export const metadata: Metadata = pageMetadata('en', 'legal');

export default function EnglishLegalNoticePage(): JSX.Element {
    return <LegalNoticePageContent locale='en' />;
}

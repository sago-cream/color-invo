import type { JSX } from 'react';
import type { Metadata } from 'next';

import { pageMetadata } from '../i18n';
import { LegalNoticePageContent } from '../LegalNoticePage';

export const metadata: Metadata = pageMetadata('zh', 'legal');

export default function LegalNoticePage(): JSX.Element {
    return <LegalNoticePageContent locale='zh' />;
}

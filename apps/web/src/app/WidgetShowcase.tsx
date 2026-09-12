'use client';

import type { JSX } from 'react';
import { useState } from 'react';
import Image from 'next/image';

interface WidgetOption {
    readonly alt: string;
    readonly label: string;
    readonly src: string;
}

interface WidgetShowcaseProps {
    readonly label: string;
    readonly options: readonly WidgetOption[];
}

export function WidgetShowcase({
    label,
    options,
}: WidgetShowcaseProps): JSX.Element {
    const [selectedIndex, setSelectedIndex] = useState(0);
    const selectedOption = options[selectedIndex] ?? options[0];

    return (
        <aside aria-label={label} className='widgetShowcase'>
            <div aria-label={label} className='widgetShowcase__picker'>
                {options.map((option, index) => (
                    <button
                        aria-pressed={selectedIndex === index}
                        className='widgetShowcase__option'
                        key={option.src}
                        onClick={() => {
                            setSelectedIndex(index);
                        }}
                        type='button'
                    >
                        {option.label}
                    </button>
                ))}
            </div>
            <div
                aria-label={selectedOption.alt}
                aria-live='polite'
                className='widgetShowcase__media'
                role='img'
            >
                <div
                    className='widgetShowcase__track'
                    style={{
                        transform: `translate3d(-${selectedIndex * 100}%, 0, 0)`,
                    }}
                >
                    {options.map((option, index) => (
                        <div
                            aria-hidden='true'
                            className='widgetShowcase__slide'
                            key={option.src}
                        >
                            <Image
                                alt=''
                                className='widgetShowcase__image'
                                height={654}
                                priority={index === 0}
                                src={option.src}
                                width={1140}
                            />
                        </div>
                    ))}
                </div>
            </div>
        </aside>
    );
}

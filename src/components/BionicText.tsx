/**
 * CareerForge - Career Acceleration Platform for BTech Engineering Students
 * Copyright (C) 2026 CareerForge Contributors
 *
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
 * See LICENSE file in the project root for full license information.
 */

import React, { useMemo } from 'react';
import { Text, TextProps } from 'react-native';

export interface BionicTextProps extends TextProps {
  children: string;
  boldRatio?: number; // Default 0.45 (bolds initial 45% of each word)
  className?: string;
  boldClassName?: string;
  regularClassName?: string;
}

/**
 * BionicText Component
 * Formats incoming text by applying weighted bold spans to word prefixes.
 * Designed explicitly for ADHD, neurodivergent, or burnt-out engineering students
 * to rapidly parse complex DSA explanations and job descriptions.
 */
export const BionicText: React.FC<BionicTextProps> = ({
  children,
  boldRatio = 0.45,
  className = 'text-slate-200 text-sm leading-relaxed',
  boldClassName = 'font-extrabold text-white',
  regularClassName = 'font-normal text-slate-300',
  ...rest
}) => {
  const parsedSpans = useMemo(() => {
    if (!children || typeof children !== 'string') return [];

    const tokens = children.split(/(\s+|[^\w\s]+)/);

    return tokens.map((token, tokenIdx) => {
      // Check if pure word
      if (/^\w+$/.test(token)) {
        const len = token.length;
        let boldLen = 1;
        if (len <= 3) {
          boldLen = 1;
        } else if (len <= 5) {
          boldLen = 2;
        } else {
          boldLen = Math.max(1, Math.ceil(len * boldRatio));
        }

        const prefix = token.substring(0, boldLen);
        const suffix = token.substring(boldLen);

        return (
          <React.Fragment key={`token-${tokenIdx}`}>
            <Text className={boldClassName}>{prefix}</Text>
            {suffix.length > 0 && <Text className={regularClassName}>{suffix}</Text>}
          </React.Fragment>
        );
      }

      // Whitespace or punctuation
      return (
        <Text key={`token-${tokenIdx}`} className={regularClassName}>
          {token}
        </Text>
      );
    });
  }, [children, boldRatio, boldClassName, regularClassName]);

  return (
    <Text className={className} accessibilityRole="text" {...rest}>
      {parsedSpans}
    </Text>
  );
};

export default BionicText;

/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import { Box, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import {
    ReactMarkdown,
    ReactMarkdownOptions,
} from "react-markdown/lib/react-markdown";
import { LinkV1 } from "../../platform/components";
import type { ParseMarkdownProps } from "./parse-markdown.interfaces";

export const ParseMarkdown: FunctionComponent<ParseMarkdownProps> = ({
    children,
    customOptions,
}) => {
    const options: ReactMarkdownOptions = {
        components: {
            h1: ({ children }) => (
                <Typography variant="h1">{children}</Typography>
            ),
            h2: ({ children }) => (
                <Typography variant="h2">{children}</Typography>
            ),
            h3: ({ children }) => (
                <Typography variant="h3">{children}</Typography>
            ),
            h4: ({ children }) => (
                <Typography variant="h4">{children}</Typography>
            ),
            h5: ({ children }) => (
                <Typography variant="h5">{children}</Typography>
            ),
            h6: ({ children }) => (
                <Typography variant="h6">{children}</Typography>
            ),
            a: ({ children, href, ...props }) => (
                <LinkV1
                    {...props}
                    externalLink
                    color="primary"
                    href={href}
                    target="_blank"
                    variant="body2"
                >
                    {children}
                </LinkV1>
            ),
            strong: ({ children }) => (
                <Box component="strong" fontWeight={600}>
                    {children}
                </Box>
            ),
        },
        children,
        ...customOptions,
    };

    return <ReactMarkdown {...options} />;
};

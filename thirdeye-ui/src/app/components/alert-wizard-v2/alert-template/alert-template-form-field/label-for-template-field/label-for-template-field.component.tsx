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
import { Box, Tooltip, Typography, useTheme } from "@material-ui/core";
import InfoIconOutlined from "@material-ui/icons/InfoOutlined";
import React, { FunctionComponent, useMemo } from "react";
import { LinkV1 } from "../../../../../platform/components";
import { ParseMarkdown } from "../../../../parse-markdown/parse-markdown.component";
import { ParseMarkdownProps } from "../../../../parse-markdown/parse-markdown.interfaces";
import { LabelForTemplateFieldProps } from "./label-for-template-field.interfaces";

export const LabelForTemplateField: FunctionComponent<LabelForTemplateFieldProps> =
    ({ name, tooltipText }) => {
        const theme = useTheme();

        const parseMarkdownProps = useMemo<
            Omit<ParseMarkdownProps, "children">
        >(
            () => ({
                customOptions: {
                    components: {
                        a: ({ children, ...props }) => (
                            <LinkV1
                                {...props}
                                externalLink
                                color="primary"
                                style={{
                                    // Using the light color to improve contrast
                                    // against the dark tooltip background
                                    color: theme.palette.primary.light,
                                }}
                                target="_blank"
                                variant="caption"
                            >
                                {children}
                            </LinkV1>
                        ),
                    },
                },
            }),
            []
        );

        return (
            <Box
                alignItems="center"
                display="flex"
                gridGap={8}
                paddingBottom={1}
                paddingTop={1}
            >
                <Typography variant="body2">{name}</Typography>
                {!!tooltipText && (
                    <Tooltip
                        arrow
                        interactive
                        placement="top"
                        title={
                            <Typography variant="caption">
                                <ParseMarkdown {...parseMarkdownProps}>
                                    {tooltipText}
                                </ParseMarkdown>
                            </Typography>
                        }
                    >
                        <InfoIconOutlined color="secondary" fontSize="small" />
                    </Tooltip>
                )}
            </Box>
        );
    };

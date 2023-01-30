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
import {
    Box,
    TextField,
    Tooltip,
    Typography,
    useTheme,
} from "@material-ui/core";
import InfoIconOutlined from "@material-ui/icons/InfoOutlined";
import React, { FunctionComponent, useMemo } from "react";
import { LinkV1 } from "../../../../platform/components";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { ParseMarkdown } from "../../../parse-markdown/parse-markdown.component";
import { ParseMarkdownProps } from "../../../parse-markdown/parse-markdown.interfaces";
import { AlertTemplateFormFieldProps } from "./alert-template-form-field.interfaces";

export const AlertTemplateFormField: FunctionComponent<AlertTemplateFormFieldProps> =
    ({ item, textFieldProps, tooltipText }) => {
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
            <InputSection
                gridContainerProps={{ alignItems: "flex-start" }}
                inputComponent={
                    <TextField
                        fullWidth
                        data-testid={`textfield-${item.key}`}
                        defaultValue={item.value}
                        {...textFieldProps}
                    />
                }
                key={item.key}
                labelComponent={
                    <Box
                        alignItems="center"
                        display="flex"
                        gridGap={8}
                        paddingBottom={1}
                        paddingTop={1}
                    >
                        <Typography variant="body2">{item.key}</Typography>
                        {tooltipText ? (
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
                                <InfoIconOutlined
                                    color="secondary"
                                    fontSize="small"
                                />
                            </Tooltip>
                        ) : null}
                    </Box>
                }
            />
        );
    };

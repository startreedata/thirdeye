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
    Grid,
    Switch,
    TextField,
    Tooltip,
    Typography,
    useTheme,
} from "@material-ui/core";
import InfoIconOutlined from "@material-ui/icons/InfoOutlined";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { JSONEditorV1, LinkV1 } from "../../../../platform/components";
import { useEventMetadataFormStyles } from "../../../event-wizard/event-metadata-form/event-metadata-form.styles";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { ParseMarkdown } from "../../../parse-markdown/parse-markdown.component";
import { ParseMarkdownProps } from "../../../parse-markdown/parse-markdown.interfaces";
import { AlertTemplateFormFieldProps } from "./alert-template-form-field.interfaces";

export const AlertTemplateFormField: FunctionComponent<AlertTemplateFormFieldProps> =
    ({ item, tabIndex, placeholder, tooltipText, onChange }) => {
        const { t } = useTranslation();
        const classes = useEventMetadataFormStyles();
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

        const label = useMemo(() => {
            return (
                <Box
                    alignItems="center"
                    display="flex"
                    gridGap={8}
                    paddingBottom={1}
                    paddingTop={1}
                >
                    <Typography variant="body2">{item.key}</Typography>
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
                            <InfoIconOutlined
                                color="secondary"
                                fontSize="small"
                            />
                        </Tooltip>
                    )}
                </Box>
            );
        }, [parseMarkdownProps, tooltipText, item]);

        let inputComponent;

        // If value of property should one or multiple predefined values
        if (item.metadata.options && item.metadata.options.length > 0) {
            inputComponent = (
                <Autocomplete
                    fullWidth
                    multiple={item.metadata.multiselect}
                    options={item.metadata.options}
                    renderInput={(params) => (
                        <TextField
                            data-testid={`optionedselect-${item.key}`}
                            {...params}
                            InputProps={{
                                ...params.InputProps,
                                // Override class name so the size of input is smaller
                                className: classes.input,
                                tabIndex: tabIndex,
                            }}
                            placeholder={
                                placeholder
                                    ? placeholder
                                    : t(
                                          "message.click-to-open-list-of-available-options"
                                      )
                            }
                            variant="outlined"
                        />
                    )}
                    value={item.value}
                    onChange={(_, selected) => {
                        onChange && onChange(selected as string[]);
                    }}
                />
            );

            // If value of item is some JSON object
        } else if (
            item.metadata.jsonType === "OBJECT" ||
            item.key === "enumerationItems"
        ) {
            inputComponent = (
                <JSONEditorV1
                    hideValidationSuccessIcon
                    data-testid={`jsoneditor-${item.key}`}
                    value={item.value ?? {}}
                    onChange={(json) => {
                        onChange && onChange(JSON.parse(json));
                    }}
                />
            );

            // If value of item is arbitrary list of strings
        } else if (item.metadata.jsonType === "ARRAY") {
            inputComponent = (
                <Autocomplete
                    freeSolo
                    fullWidth
                    multiple
                    options={[]}
                    renderInput={(params) => (
                        <TextField
                            data-testid={`multifreesolo-${item.key}`}
                            {...params}
                            InputProps={{
                                ...params.InputProps,
                                // Override class name so the size of input is smaller
                                className: classes.input,
                            }}
                            inputProps={{
                                tabIndex: tabIndex,
                                "data-testid": `multifreesolo-input-${item.key}`,
                            }}
                            placeholder={
                                placeholder
                                    ? placeholder
                                    : t(
                                          "message.type-a-value-and-press-enter-to-add"
                                      )
                            }
                            variant="outlined"
                        />
                    )}
                    value={item.value as string[]}
                    onChange={(_, selected) => {
                        onChange && onChange(selected as string[]);
                    }}
                />
            );
            // If value of item is a boolean value
        } else if (item.metadata.jsonType === "BOOLEAN") {
            inputComponent = (
                <Grid
                    container
                    alignItems="center"
                    component="label"
                    justifyContent="space-around"
                    spacing={1}
                >
                    <Grid item>{t("label.false")}</Grid>
                    <Grid item>
                        <Switch
                            data-testid={`switch-${item.key}`}
                            defaultChecked={!!item.value}
                            onChange={(_, checked) => {
                                onChange && onChange(checked);
                            }}
                        />
                    </Grid>
                    <Grid item>{t("label.true")}</Grid>
                </Grid>
            );
            // Default to string input field
        } else {
            inputComponent = (
                <TextField
                    fullWidth
                    data-testid={`textfield-${item.key}`}
                    defaultValue={item.value}
                    inputProps={{
                        tabIndex: tabIndex,
                        "data-testid": `input-${item.key}`,
                    }}
                    placeholder={placeholder}
                    onChange={(e) => {
                        onChange && onChange(e.currentTarget.value);
                    }}
                />
            );
        }

        return (
            <InputSection
                gridContainerProps={{ alignItems: "flex-start" }}
                inputComponent={inputComponent}
                key={item.key}
                labelComponent={label}
            />
        );
    };

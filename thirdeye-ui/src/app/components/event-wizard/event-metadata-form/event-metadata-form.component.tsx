/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Box,
    Button,
    Chip,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    EventMetadataFormProps,
    PropertyData,
} from "./event-metadata-form.interfaces";
import { useEventMetadataFormStyles } from "./event-metadata-form.styles";

export const EventMetadataForm: FunctionComponent<EventMetadataFormProps> = ({
    initialPropertiesData,
    onChange,
}) => {
    const classes = useEventMetadataFormStyles();
    const { t } = useTranslation();
    const [propertiesData, setPropertiesData] = useState<PropertyData[]>(
        initialPropertiesData
    );

    const handleKeyChange = (
        newKeyString: string,
        item: PropertyData
    ): void => {
        item.propertyName = newKeyString;

        const cloned = [...propertiesData];

        setPropertiesData(cloned);

        onChange(cloned);
    };

    const handleAddListItem = (): void => {
        const newItem = {
            originalKey: null,
            propertyName: "",
            propertyValue: [],
        };
        setPropertiesData((prev) => [...prev, newItem]);
    };

    const handleStringValueChange = (
        newValue: string[],
        item: PropertyData
    ): void => {
        item.propertyValue = newValue;
        onChange(propertiesData);
    };

    return (
        <Grid container alignItems="center">
            <Grid item xs={12}>
                <Box marginBottom={2}>
                    <Typography variant="h5">
                        {t("label.event-metadata")}
                    </Typography>
                    <Typography variant="body2">
                        {t("message.create-custom-event-properties-and-values")}
                    </Typography>
                </Box>
            </Grid>

            <Grid item xs={6}>
                <Box paddingBottom={1}>{t("label.property-name")}</Box>
            </Grid>
            <Grid item xs={6}>
                <Box paddingBottom={1}>{t("label.property-value")}</Box>
            </Grid>

            <Grid item xs={12}>
                {propertiesData.map((item, idx) => {
                    return (
                        <Grid container item key={idx.toString()} xs={12}>
                            <Grid item xs={5}>
                                <TextField
                                    fullWidth
                                    inputProps={{ tabIndex: -1 }}
                                    name="propertyName"
                                    placeholder={t("label.add-property-key")}
                                    value={item.propertyName}
                                    onChange={(
                                        e: React.ChangeEvent<
                                            | HTMLTextAreaElement
                                            | HTMLInputElement
                                        >
                                    ) =>
                                        handleKeyChange(
                                            e.currentTarget.value,
                                            item
                                        )
                                    }
                                />
                            </Grid>
                            <Grid item xs={1} />
                            <Grid item xs={5}>
                                <Autocomplete
                                    freeSolo
                                    multiple
                                    defaultValue={item.propertyValue}
                                    options={[] as string[]}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            InputProps={{
                                                ...params.InputProps,
                                                /**
                                                 * Override class name so the
                                                 * size of input is smaller
                                                 */
                                                className: classes.input,
                                            }}
                                            name="propertyValue"
                                            placeholder={t(
                                                "message.property-value-placeholder"
                                            )}
                                            variant="outlined"
                                        />
                                    )}
                                    renderTags={(
                                        value: readonly string[],
                                        getTagProps
                                    ) =>
                                        value.map(
                                            (option: string, index: number) => (
                                                <Chip
                                                    key={index}
                                                    variant="outlined"
                                                    {...getTagProps({
                                                        index,
                                                    })}
                                                    label={option}
                                                    size="small"
                                                />
                                            )
                                        )
                                    }
                                    onChange={(
                                        _event: React.ChangeEvent<
                                            Record<string, unknown>
                                        >,
                                        value: string[]
                                    ) => {
                                        handleStringValueChange(value, item);
                                    }}
                                />
                            </Grid>

                            {/* Spacer */}
                            <Grid item sm={12} />
                        </Grid>
                    );
                })}
                <Grid item xs={12}>
                    <Button variant="contained" onClick={handleAddListItem}>
                        {t("label.add-metadata-entry")}
                    </Button>
                </Grid>
            </Grid>
        </Grid>
    );
};

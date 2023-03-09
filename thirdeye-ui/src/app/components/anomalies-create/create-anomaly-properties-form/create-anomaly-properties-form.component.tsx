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
    List,
    ListItem,
    ListItemText,
    Paper,
    TextField,
    Typography,
} from "@material-ui/core";
import InfoOutlined from "@material-ui/icons/InfoOutlined";
import { Autocomplete } from "@material-ui/lab";
import { capitalize } from "lodash";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../platform/rest/actions.interfaces";
import { linkRendererV1 } from "../../../platform/utils";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";
import { generateNameForEnumerationItem } from "../../../utils/enumeration-items/enumeration-items.util";
import { getAlertsAlertViewPath } from "../../../utils/routes/routes.util";
import { createTimeRangeDuration } from "../../../utils/time-range/time-range.util";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeButton } from "../../time-range/time-range-button/time-range-button.component";
import { TimeRange } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { CreateAnomalyFormKeys } from "../create-anomaly-wizard/create-anomaly-wizard.interfaces";
import { getEnumerationItemsConfigFromAlert } from "../create-anomaly-wizard/create-anomaly-wizard.utils";
import { CreateAnomalyPropertiesFormProps } from "./create-anomaly-properties-form.interfaces";

export const CreateAnomalyPropertiesForm: FunctionComponent<CreateAnomalyPropertiesFormProps> =
    ({
        alerts,
        formFields,
        selectedAlertDetails,
        handleSetField,
        enumerationItemsForAlert,
        enumerationItemsStatus,
        timezone,
    }) => {
        const { t } = useTranslation();

        const showEnumerationItemsField = !!(
            formFields.alert &&
            getEnumerationItemsConfigFromAlert(formFields.alert)
        );

        const formLabels: Record<CreateAnomalyFormKeys, string> = {
            alert: t("label.alert"),
            dateRange: t("label.date-range"),
            enumerationItem: t("label.dimension"),
        };

        const timeRangeDuration = createTimeRangeDuration(
            TimeRange.CUSTOM,
            formFields.dateRange[0],
            formFields.dateRange[1]
        );

        const alertDetails = useMemo<string[][] | null>(() => {
            if (!formFields.alert) {
                return null;
            }
            const details: string[][] = [];

            if (formFields.alert?.description) {
                details.push([
                    t("label.description"),
                    formFields.alert?.description,
                ]);
            }
            if (selectedAlertDetails?.dataSource) {
                details.push([
                    t("label.datasource"),
                    selectedAlertDetails?.dataSource,
                ]);
            }
            if (selectedAlertDetails?.dataset) {
                details.push([
                    t("label.dataset"),
                    selectedAlertDetails?.dataset,
                ]);
            }
            if (selectedAlertDetails?.metric) {
                details.push([t("label.metric"), selectedAlertDetails?.metric]);
            }

            return details;
        }, [formFields.alert]);

        return (
            <Grid container justifyContent="space-between">
                <Grid item lg={6} md={8} xs={12}>
                    <InputSection
                        fullWidth
                        inputComponent={
                            <Autocomplete
                                disableClearable
                                getOptionLabel={(option) => option.name}
                                options={alerts}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{
                                            ...params.InputProps,
                                        }}
                                        margin="dense"
                                        placeholder={capitalize(
                                            t(
                                                "message.click-here-to-select-entity",
                                                {
                                                    entity: t("label.alert"),
                                                }
                                            )
                                        )}
                                        size="small"
                                        variant="outlined"
                                    />
                                )}
                                value={formFields.alert || undefined}
                                onChange={(_, selectedValue) => {
                                    selectedValue &&
                                        handleSetField("alert", selectedValue);
                                }}
                            />
                        }
                        labelComponent={
                            <>
                                <Typography variant="body2">
                                    {formLabels.alert}
                                </Typography>
                                {!!formFields.alert &&
                                    linkRendererV1(
                                        t("label.view-entity", {
                                            entity: t("label.alert"),
                                        }),
                                        getAlertsAlertViewPath(
                                            formFields.alert.id
                                        ),
                                        false,
                                        undefined,
                                        true,
                                        "_blank"
                                    )}
                            </>
                        }
                    />
                    <LoadingErrorStateSwitch
                        isError={enumerationItemsStatus === ActionStatus.Error}
                        isLoading={
                            enumerationItemsStatus === ActionStatus.Working
                        }
                        loadingState={
                            <InputSection
                                fullWidth
                                inputComponent={
                                    <SkeletonV1
                                        height={50}
                                        variant="rect"
                                        width="100%"
                                    />
                                }
                                label={formLabels.enumerationItem}
                            />
                        }
                    >
                        {!!(
                            showEnumerationItemsField &&
                            enumerationItemsForAlert &&
                            enumerationItemsForAlert.length > 0
                        ) && (
                            <InputSection
                                fullWidth
                                inputComponent={
                                    <Autocomplete<EnumerationItem>
                                        getOptionLabel={(option) =>
                                            generateNameForEnumerationItem(
                                                option
                                            )
                                        }
                                        options={enumerationItemsForAlert}
                                        renderInput={(params) => (
                                            <TextField
                                                {...params}
                                                placeholder={capitalize(
                                                    t(
                                                        "message.click-here-to-select-entity",
                                                        {
                                                            entity: t(
                                                                "label.dimension"
                                                            ),
                                                        }
                                                    )
                                                )}
                                                variant="outlined"
                                            />
                                        )}
                                        size="small"
                                        value={formFields.enumerationItem}
                                        onChange={(_, selectedValue) => {
                                            handleSetField(
                                                "enumerationItem",
                                                selectedValue
                                            );
                                        }}
                                    />
                                }
                                label={formLabels.enumerationItem}
                            />
                        )}
                    </LoadingErrorStateSwitch>

                    {/* Only show the datetime picker when the timezone prop is passed, 
                to have the timezone shown be relevant to the selected alert  */}
                    {!!timezone && (
                        <InputSection
                            fullWidth
                            helperLabel={t(
                                "message.select-the-start-and-end-date-time-range-for-the-anomalous-behavior"
                            )}
                            inputComponent={
                                <TimeRangeButton
                                    timeRangeDuration={timeRangeDuration}
                                    timezone={timezone}
                                    onChange={({ startTime, endTime }) =>
                                        handleSetField("dateRange", [
                                            startTime,
                                            endTime,
                                        ])
                                    }
                                />
                            }
                            label={formLabels.dateRange}
                        />
                    )}
                </Grid>
                <Grid item lg={4} md={4} xs={12}>
                    {!!alertDetails && (
                        <Paper variant="outlined">
                            <Box pb={0} pt={3} px={3}>
                                <Box
                                    clone
                                    alignItems="center"
                                    display="flex"
                                    gridGap={6}
                                >
                                    <Typography
                                        color="textSecondary"
                                        variant="body1"
                                    >
                                        <InfoOutlined />
                                        {t("label.alert-details")}
                                    </Typography>
                                </Box>
                                <List dense>
                                    {alertDetails.map(([label, value]) => (
                                        <ListItem
                                            dense
                                            disableGutters
                                            key={label}
                                        >
                                            <ListItemText
                                                primary={label}
                                                secondary={value}
                                            />
                                        </ListItem>
                                    ))}
                                </List>
                            </Box>
                        </Paper>
                    )}
                </Grid>
            </Grid>
        );
    };

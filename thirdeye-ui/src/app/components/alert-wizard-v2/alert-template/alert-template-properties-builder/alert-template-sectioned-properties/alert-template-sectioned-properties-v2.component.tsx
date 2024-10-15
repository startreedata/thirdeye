/*
 * Copyright 2024 StarTree Inc
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
import { Box, Grid, Typography, withStyles } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    extendablePropertyStepNameMapV2,
    ALERT_PROPERTIES_DEFAULT_ORDER,
    ALERT_PROPERTIES_DEFAULT_STEP,
} from "../../../../../utils/alerts/alerts.util";
import { AlertTemplateFormField } from "../../alert-template-form-field/alert-template-form-field-v2.component";
import { AlertTemplateSectionedPropertiesProps } from "./alert-template-sectioned-properties.interfaces";
import { PropertyRenderConfig } from "../alert-template-properties-builder.interfaces";
import { isEmpty } from "lodash";
import { easyAlertStyles } from "../../../../../pages/alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.styles";

const StyledGrid = withStyles((theme) => ({
    root: {
        paddingLeft: theme.spacing(1),
    },
}))(Grid);

export const AlertTemplateSectionedProperties: FunctionComponent<AlertTemplateSectionedPropertiesProps> =
    ({ groupedProperties, handlePropertyValueChange }) => {
        const { t } = useTranslation();
        const classes = easyAlertStyles();

        const orderedGroupedProperties: Record<
            string,
            Record<string, PropertyRenderConfig[]>
        > = {};
        /* The idea is to display properties in a specifc order. We have that order pre-defined.
            In case there is a property that belongs to a step that is not in pre-defined order,
            we push it at the second last place, just before the properties of "OTHER" step
        */
        const keysPresent = Object.keys(groupedProperties);
        ALERT_PROPERTIES_DEFAULT_ORDER.forEach((alertProperty) => {
            if (alertProperty !== ALERT_PROPERTIES_DEFAULT_STEP.STEP) {
                if (groupedProperties[alertProperty]) {
                    orderedGroupedProperties[alertProperty] =
                        groupedProperties[alertProperty];
                }
            }
        });
        const keysNotInDefaultOrder = keysPresent.filter(
            (key) => !ALERT_PROPERTIES_DEFAULT_ORDER.includes(key)
        );
        if (!isEmpty(keysNotInDefaultOrder)) {
            keysNotInDefaultOrder.forEach((key) => {
                orderedGroupedProperties[key] = groupedProperties[key];
            });
        }
        if (groupedProperties[ALERT_PROPERTIES_DEFAULT_STEP.STEP]) {
            orderedGroupedProperties[ALERT_PROPERTIES_DEFAULT_STEP.STEP] =
                groupedProperties[ALERT_PROPERTIES_DEFAULT_STEP.STEP];
        }

        return (
            <StyledGrid container>
                {Object.keys(orderedGroupedProperties).map((step, stepIdx) => {
                    const subStepMap = groupedProperties[step];

                    return (
                        <Grid item key={`existing-${step}`} xs={12}>
                            <Box paddingBottom={2}>
                                <Typography
                                    className={classes.inputHeader}
                                    variant="h6"
                                >
                                    {extendablePropertyStepNameMapV2[step]}
                                </Typography>
                                <Grid container>
                                    {subStepMap?.["DIRECT"]?.map(
                                        (directSubStep, directSubStepIdx) => (
                                            <AlertTemplateFormField
                                                defaultValue={
                                                    directSubStep.metadata
                                                        .defaultValue
                                                }
                                                item={directSubStep}
                                                key={directSubStep.key}
                                                tabIndex={directSubStepIdx + 1}
                                                tooltipText={
                                                    directSubStep.metadata
                                                        .description
                                                }
                                                onChange={(selected) =>
                                                    handlePropertyValueChange(
                                                        directSubStep.key,
                                                        selected
                                                    )
                                                }
                                            />
                                        )
                                    )}
                                </Grid>

                                {Object.keys(subStepMap)
                                    .slice(1)
                                    .filter((cs) => subStepMap[cs].length > 0)
                                    .map((currentSubStep) => (
                                        <Grid
                                            item
                                            key={`required-${step}-${currentSubStep}`}
                                            xs={12}
                                        >
                                            <Box
                                                paddingBottom={2}
                                                paddingTop={2}
                                            >
                                                <Typography variant="h6">
                                                    {currentSubStep}
                                                </Typography>
                                                <Grid container>
                                                    {subStepMap[
                                                        currentSubStep
                                                    ].map(
                                                        (
                                                            property,
                                                            propertyIdx
                                                        ) => (
                                                            <AlertTemplateFormField
                                                                item={property}
                                                                key={
                                                                    property.key
                                                                }
                                                                placeholder={t(
                                                                    "label.add-property-value",
                                                                    {
                                                                        key: property.key,
                                                                    }
                                                                )}
                                                                tabIndex={
                                                                    propertyIdx +
                                                                    1 +
                                                                    stepIdx
                                                                }
                                                                tooltipText={
                                                                    property
                                                                        .metadata
                                                                        .description
                                                                }
                                                                onChange={(
                                                                    selected
                                                                ) =>
                                                                    handlePropertyValueChange(
                                                                        property.key,
                                                                        selected
                                                                    )
                                                                }
                                                            />
                                                        )
                                                    )}
                                                </Grid>
                                            </Box>
                                        </Grid>
                                    ))}
                            </Box>
                        </Grid>
                    );
                })}
            </StyledGrid>
        );
    };

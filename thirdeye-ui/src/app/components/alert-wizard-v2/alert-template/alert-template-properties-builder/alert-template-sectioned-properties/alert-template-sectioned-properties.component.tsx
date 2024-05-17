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
    existingPropertySteps,
    isStepEmpty,
    propertyStepNameMap,
} from "../../../../../utils/alerts/alerts.util";
import { AlertTemplateFormField } from "../../alert-template-form-field/alert-template-form-field.component";
import { AlertTemplateSectionedPropertiesProps } from "./alert-template-sectioned-properties.interfaces";

const StyledGrid = withStyles((theme) => ({
    root: {
        paddingLeft: theme.spacing(1),
    },
}))(Grid);

export const AlertTemplateSectionedProperties: FunctionComponent<AlertTemplateSectionedPropertiesProps> =
    ({ groupedProperties, handlePropertyValueChange }) => {
        const { t } = useTranslation();

        return (
            <StyledGrid container>
                {existingPropertySteps.map((step, stepIdx) => {
                    const subStepMap = groupedProperties[step];
                    if (isStepEmpty(step, groupedProperties)) {
                        return null;
                    }

                    return (
                        <Grid item key={`existing-${step}`} xs={12}>
                            <Box paddingBottom={2}>
                                <Typography variant="h5">
                                    {propertyStepNameMap[step]}
                                </Typography>
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
                                {Object.keys(subStepMap)
                                    .slice(1)
                                    .map((currentSubStep) => {
                                        const isSubStepEmpty =
                                            subStepMap[currentSubStep]
                                                .length === 0;
                                        if (isSubStepEmpty) {
                                            return null;
                                        }

                                        return (
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
                                                </Box>
                                            </Grid>
                                        );
                                    })}
                            </Box>
                        </Grid>
                    );
                })}
            </StyledGrid>
        );
    };

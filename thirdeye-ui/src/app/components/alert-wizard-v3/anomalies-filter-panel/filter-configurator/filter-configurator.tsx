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
    Card,
    CardContent,
    Grid,
    Switch,
    Tooltip,
    Typography,
} from "@material-ui/core";
import InfoIconOutlined from "@material-ui/icons/InfoOutlined";
import { every } from "lodash";
import React, { FunctionComponent, useState } from "react";
import {
    PropertyConfigValueTypes,
    TemplatePropertiesObject,
} from "../../../../rest/dto/alert.interfaces";
import { FormComponentForTemplateField } from "../../../alert-wizard-v2/alert-template/alert-template-form-field/form-component-for-template-field/form-component-for-template-field.component";
import { LabelForTemplateField } from "../../../alert-wizard-v2/alert-template/alert-template-form-field/label-for-template-field/label-for-template-field.component";
import { FilterConfiguratorProps } from "./filter-configurator.interfaces";

export const FilterConfigurator: FunctionComponent<FilterConfiguratorProps> = ({
    renderConfig,
    alert,
    onAlertPropertyChange,
}) => {
    const [localCopyOfProperties, setLocalCopyOfProperties] =
        useState<TemplatePropertiesObject>(() => {
            const configurationForFilter: TemplatePropertiesObject = {};

            renderConfig.requiredPropertiesWithMetadata.forEach(
                ({ name: k, defaultValue }) => {
                    configurationForFilter[k] =
                        alert.templateProperties[k] ||
                        (defaultValue as PropertyConfigValueTypes);
                }
            );

            return configurationForFilter;
        });
    const [isOn, setIsOn] = useState(() => {
        return every(
            renderConfig.requiredPropertiesWithMetadata.map(
                ({ name: k, defaultValue }) => {
                    let isSet = true;

                    if (defaultValue !== undefined) {
                        isSet = localCopyOfProperties[k] !== defaultValue;
                    }

                    isSet = isSet && localCopyOfProperties[k] !== undefined;

                    return isSet;
                }
            )
        );
    });

    const handleOnChange = (
        propertyName: string,
        newValue: PropertyConfigValueTypes
    ): void => {
        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                [propertyName]: newValue,
            },
        });
        setLocalCopyOfProperties((current) => {
            current[propertyName] = newValue;

            return current;
        });
    };

    const handleOnOffClick = (newFlag: boolean): void => {
        if (!newFlag) {
            const filtersRemoved = {
                ...alert.templateProperties,
            };

            renderConfig.requiredPropertiesWithMetadata.forEach(
                ({ name: k }) => {
                    delete filtersRemoved[k];
                }
            );

            onAlertPropertyChange({
                templateProperties: filtersRemoved,
            });
        } else {
            onAlertPropertyChange({
                templateProperties: {
                    ...alert.templateProperties,
                    ...localCopyOfProperties,
                },
            });
        }

        setIsOn(newFlag);
    };

    return (
        <Card variant="elevation">
            <CardContent>
                <Grid container alignItems="center">
                    <Grid item>
                        <Switch
                            checked={isOn}
                            color="primary"
                            onChange={(_, newFlag: boolean) =>
                                handleOnOffClick(newFlag)
                            }
                        />
                    </Grid>
                    <Grid item>{renderConfig.name}</Grid>
                    {renderConfig.description && (
                        <Grid item>
                            <Tooltip
                                arrow
                                interactive
                                placement="top"
                                title={
                                    <Typography variant="caption">
                                        {renderConfig.description}
                                    </Typography>
                                }
                            >
                                <InfoIconOutlined
                                    color="secondary"
                                    fontSize="small"
                                />
                            </Tooltip>
                        </Grid>
                    )}
                </Grid>
            </CardContent>
            {isOn && (
                <CardContent>
                    {renderConfig.requiredPropertiesWithMetadata.map(
                        (propertyMetadata) => {
                            return (
                                <Box key={propertyMetadata.name}>
                                    <LabelForTemplateField
                                        name={propertyMetadata.name}
                                        tooltipText={
                                            propertyMetadata.description
                                        }
                                    />
                                    <FormComponentForTemplateField
                                        propertyKey={propertyMetadata.name}
                                        templateFieldProperty={propertyMetadata}
                                        value={
                                            localCopyOfProperties[
                                                propertyMetadata.name
                                            ]
                                        }
                                        onChange={(newValue) => {
                                            handleOnChange(
                                                propertyMetadata.name,
                                                newValue
                                            );
                                        }}
                                    />
                                </Box>
                            );
                        }
                    )}
                </CardContent>
            )}
        </Card>
    );
};

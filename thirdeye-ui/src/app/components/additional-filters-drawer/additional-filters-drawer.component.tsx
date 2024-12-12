/*
 * Copyright 2023 StarTree Inc
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

import { Icon } from "@iconify/react";
import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Box,
    Button,
    Drawer,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    PropertyConfigValueTypes,
    TemplatePropertiesObject,
} from "../../rest/dto/alert.interfaces";
import { FormComponentForTemplateField } from "../alert-wizard-v2/alert-template/alert-template-form-field/form-component-for-template-field/form-component-for-template-field.component";
import { LabelForTemplateFieldV2 } from "../alert-wizard-v2/alert-template/alert-template-form-field/label-for-template-field-v2/label-for-template-field-v2.component";
import { AdditonalFiltersDrawerProps } from "./additional-filters-drawer.interfaces";
import { useAdditonalFiltersDrawerStyles } from "./additional-filters-drawer.styles";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";

// import { uniq } from "lodash";

/**
 * Convenience wrapper on top of HelpDrawerCoreV1 so consumers do not have
 * to maintain isOpen state
 */
export const AdditonalFiltersDrawer: FunctionComponent<AdditonalFiltersDrawerProps> =
    ({
        defaultValues,
        onApply,
        isOpen,
        onClose,
        availableConfigurations,
        emptyMessage,
    }) => {
        // const uniqueSections = uniq(availableConfigurations.map(option => option.section))
        const classes = useAdditonalFiltersDrawerStyles();
        const { t } = useTranslation();
        const [localCopyOfProperties, setLocalCopyOfProperties] =
            useState<TemplatePropertiesObject>({
                ...defaultValues,
            });

        const handleOnChange = (
            key: string,
            value: PropertyConfigValueTypes
        ): void => {
            setLocalCopyOfProperties((prev) => ({
                ...prev,
                [key]: value,
            }));
        };

        const onSubmit = (): void => {
            onApply(localCopyOfProperties);
        };

        return (
            <Drawer
                PaperProps={{
                    className: classes.drawerPaper,
                }}
                anchor="right"
                open={isOpen}
                onClose={onClose}
            >
                <form onSubmit={onSubmit}>
                    <Box
                        alignItems="center"
                        className={classes.header}
                        display="flex"
                        justifyContent="space-between"
                    >
                        <Typography variant="h6">
                            {t("label.advanced-options")}
                        </Typography>
                        <Icon
                            cursor="pointer"
                            fontSize={24}
                            icon="ic:round-close"
                            onClick={onClose}
                        />
                    </Box>
                    {emptyMessage ? (
                        emptyMessage
                    ) : (
                        <>
                            <Box className={classes.content} flex={1}>
                                <Box>
                                    {availableConfigurations.map((config) => (
                                        <Box
                                            className={classes.configItem}
                                            key={config.name}
                                            mb={3}
                                            width="100%"
                                        >
                                            <Accordion>
                                                <AccordionSummary
                                                    aria-controls={`${config.name}-content`}
                                                    expandIcon={
                                                        <ExpandMoreIcon />
                                                    }
                                                    id={`${config.name}-header`}
                                                >
                                                    <Typography
                                                        color="inherit"
                                                        variant="h6"
                                                    >
                                                        {config.name}
                                                    </Typography>
                                                </AccordionSummary>
                                                <AccordionDetails>
                                                    <Box
                                                        className={
                                                            classes.configItemFields
                                                        }
                                                    >
                                                        {config.requiredPropertiesWithMetadata.map(
                                                            (
                                                                propertyMetadata
                                                            ) => (
                                                                <Box
                                                                    key={
                                                                        propertyMetadata.name
                                                                    }
                                                                >
                                                                    <LabelForTemplateFieldV2
                                                                        className={
                                                                            classes.formLabel
                                                                        }
                                                                        name={
                                                                            propertyMetadata.name
                                                                        }
                                                                        tooltipText={
                                                                            propertyMetadata.description
                                                                        }
                                                                    />
                                                                    <FormComponentForTemplateField
                                                                        propertyKey={
                                                                            propertyMetadata.name
                                                                        }
                                                                        templateFieldProperty={
                                                                            propertyMetadata
                                                                        }
                                                                        value={
                                                                            localCopyOfProperties[
                                                                                propertyMetadata
                                                                                    .name
                                                                            ]
                                                                        }
                                                                        onChange={(
                                                                            newValue
                                                                        ) => {
                                                                            handleOnChange(
                                                                                propertyMetadata.name,
                                                                                newValue
                                                                            );
                                                                        }}
                                                                    />
                                                                </Box>
                                                            )
                                                        )}
                                                    </Box>
                                                </AccordionDetails>
                                            </Accordion>
                                        </Box>
                                    ))}
                                </Box>
                            </Box>
                            <Box
                                className={classes.footer}
                                display="flex"
                                justifyContent="space-between"
                            >
                                <Button
                                    className={classes.actionSecondary}
                                    size="medium"
                                    variant="contained"
                                    onClick={onClose}
                                >
                                    {t("label.close")}
                                </Button>
                                <Button
                                    className={classes.actionPrimary}
                                    color="primary"
                                    size="medium"
                                    type="submit"
                                    variant="contained"
                                >
                                    {t("label.apply-filter")}
                                </Button>
                            </Box>
                        </>
                    )}
                </form>
            </Drawer>
        );
    };

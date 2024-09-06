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
    Box,
    Button,
    Drawer,
    FormControl,
    FormLabel,
    TextField,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AdditonalFiltersDrawerProps } from "./additional-filters-drawer.interfaces";
import { useAdditonalFiltersDrawerStyles } from "./additional-filters-drawer.styles";

/**
 * Convenience wrapper on top of HelpDrawerCoreV1 so consumers do not have
 * to maintain isOpen state
 */
export const AdditonalFiltersDrawer: FunctionComponent<AdditonalFiltersDrawerProps> =
    ({ onApply, isOpen, onClose, availableConfigurations }) => {
        const classes = useAdditonalFiltersDrawerStyles();
        const { t } = useTranslation();

        return (
            <Drawer
                PaperProps={{
                    className: classes.drawerPaper,
                }}
                anchor="right"
                open={isOpen}
                onClose={onClose}
            >
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
                <Box className={classes.content} flex={1}>
                    <Box>
                        {availableConfigurations.map((config) => (
                            <Box key={config.name} mb={3} width="100%">
                                <Typography variant="h6">
                                    {config.name}
                                </Typography>
                                {config.requiredPropertiesWithMetadata.map(
                                    (property) => (
                                        <FormControl
                                            fullWidth
                                            key={property.name}
                                        >
                                            <FormLabel>
                                                {property.name}
                                            </FormLabel>
                                            <TextField fullWidth />
                                        </FormControl>
                                    )
                                )}
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
                        variant="contained"
                        onClick={onApply}
                    >
                        {t("label.apply-filter")}
                    </Button>
                </Box>
            </Drawer>
        );
    };

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
import { Box, Button, Drawer, Typography } from "@material-ui/core";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { SearchInputV1 } from "../../platform/components";
import { ColorV1 } from "../../platform/utils/material-ui/color.util";
import { CopyButtonV2 } from "../copy-button-v2/copy-button-v2.component";
import { ColumnsDrawerProps } from "./columns-drawer.interfaces";
import { useColumnsDrawerStyles } from "./columns-drawer.styles";

/**
 * Convenience wrapper on top of HelpDrawerCoreV1 so consumers do not have
 * to maintain isOpen state
 */
export const ColumnsDrawer: FunctionComponent<ColumnsDrawerProps> = ({
    selectedDataset,
    isOpen,
    onClose,
}) => {
    const classes = useColumnsDrawerStyles();
    const { t } = useTranslation();
    const [searchQuery, setSearchQuery] = useState("");

    const filteredColumns = useMemo(() => {
        if (!selectedDataset) {
            return [];
        }

        const filteredMetrics = selectedDataset.metrics.filter(
            (metric) =>
                metric.name.toLowerCase().includes(searchQuery.toLowerCase()) &&
                metric.name !== "*"
        );

        const filteredDimensions = selectedDataset.dimensions.filter(
            (dimension) =>
                dimension.toLowerCase().includes(searchQuery.toLowerCase())
        );

        return [...filteredMetrics, ...filteredDimensions];
    }, [selectedDataset, searchQuery]);

    return (
        <>
            <Drawer
                PaperProps={{
                    className: classes.drawerPaper,
                }}
                anchor="right"
                open={isOpen}
                onClose={onClose}
            >
                <Box display="flex" flexDirection="column" height="100%">
                    <Box
                        alignItems="center"
                        className={classes.header}
                        display="flex"
                        justifyContent="space-between"
                    >
                        <Typography variant="h6">
                            {t("label.columns")}
                        </Typography>
                        <Icon
                            cursor="pointer"
                            fontSize={24}
                            icon="ic:round-close"
                            onClick={onClose}
                        />
                    </Box>
                    <Box className={classes.content} flex={1}>
                        <Box
                            border={1}
                            borderColor={ColorV1.Grey10}
                            borderRadius={10}
                            display="flex"
                            flexDirection="column"
                            height="100%"
                            overflow="hidden"
                        >
                            <SearchInputV1
                                fullWidth
                                className={classes.searchInput}
                                placeholder={t("label.search")}
                                value={searchQuery}
                                onChange={setSearchQuery}
                                onChangeDelay={0}
                            />
                            <Box className={classes.listContainer}>
                                {filteredColumns.map((column) => {
                                    const currentDimension =
                                        typeof column === "string"
                                            ? column
                                            : column.name;

                                    return (
                                        <Box
                                            alignItems="center"
                                            className={classes.listItem}
                                            display="flex"
                                            justifyContent="space-between"
                                            key={currentDimension}
                                        >
                                            <Typography variant="body2">
                                                {currentDimension}
                                            </Typography>
                                            <CopyButtonV2
                                                content={currentDimension}
                                            />
                                        </Box>
                                    );
                                })}
                            </Box>
                        </Box>
                    </Box>
                    <Box
                        className={classes.footer}
                        display="flex"
                        justifyContent="flex-end"
                    >
                        <Button
                            className={classes.action}
                            size="medium"
                            variant="contained"
                            onClick={onClose}
                        >
                            {t("label.close")}
                        </Button>
                    </Box>
                </Box>
            </Drawer>
        </>
    );
};

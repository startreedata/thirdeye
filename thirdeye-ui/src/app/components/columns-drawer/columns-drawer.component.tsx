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
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { SearchInputV1 } from "../../platform/components";
import { ColorV1 } from "../../platform/utils/material-ui/color.util";
import { useGetDataset } from "../../rest/datasets/datasets.actions";
import { ColumnsDrawerProps } from "./columns-drawer.interfaces";
import { useColumnsDrawerStyles } from "./columns-drawer.styles";

/**
 * Convenience wrapper on top of HelpDrawerCoreV1 so consumers do not have
 * to maintain isOpen state
 */
export const ColumnsDrawer: FunctionComponent<ColumnsDrawerProps> = ({
    datasetId,
    isOpen,
    onClose,
}) => {
    const classes = useColumnsDrawerStyles();
    const { t } = useTranslation();
    const { dataset, getDataset } = useGetDataset();
    const [searchQuery, setSearchQuery] = useState("");

    const filteredDimensions = useMemo(
        () =>
            dataset?.dimensions.filter((dimension) =>
                dimension.toLowerCase().includes(searchQuery.toLowerCase())
            ) || [],
        [dataset, searchQuery]
    );

    useEffect(() => {
        getDataset(toNumber(datasetId));
    }, [datasetId]);

    const handleCopy = (text: string): void => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(text);
        }
    };

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
                            />
                            <Box className={classes.listContainer}>
                                {filteredDimensions.map((dimension) => (
                                    <Box
                                        alignItems="center"
                                        className={classes.listItem}
                                        display="flex"
                                        justifyContent="space-between"
                                        key={dimension}
                                        role="button"
                                        onClick={() => handleCopy(dimension)}
                                    >
                                        <Typography variant="body2">
                                            {dimension}
                                        </Typography>
                                        <Icon
                                            color={ColorV1.Blue8}
                                            fontSize={16}
                                            icon="bi:copy"
                                        />
                                    </Box>
                                ))}
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

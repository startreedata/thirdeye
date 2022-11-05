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
    Card,
    CardContent,
    CardHeader,
    Grid,
    IconButton,
    Link,
    Menu,
    MenuItem,
} from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    getDatasetsUpdatePath,
    getDatasetsViewPath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { DatasetCardProps } from "./dataset-card.interfaces";

export const DatasetCard: FunctionComponent<DatasetCardProps> = (
    props: DatasetCardProps
) => {
    const [datasetOptionsAnchorElement, setDatasetOptionsAnchorElement] =
        useState<HTMLElement | null>();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleDatasetOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setDatasetOptionsAnchorElement(event.currentTarget);
    };

    const handleDatasetOptionsClose = (): void => {
        setDatasetOptionsAnchorElement(null);
    };

    const handleDatasetViewDetails = (): void => {
        if (!props.uiDataset) {
            return;
        }

        navigate(getDatasetsViewPath(props.uiDataset.id));
        handleDatasetOptionsClose();
    };

    const handleDatasetEdit = (): void => {
        if (!props.uiDataset) {
            return;
        }

        navigate(getDatasetsUpdatePath(props.uiDataset.id));
        handleDatasetOptionsClose();
    };

    const handleDatasetDelete = (): void => {
        if (!props.uiDataset) {
            return;
        }

        props.onDelete && props.onDelete(props.uiDataset);
        handleDatasetOptionsClose();
    };

    return (
        <Card variant="outlined">
            {props.uiDataset && (
                <CardHeader
                    action={
                        <Grid container alignItems="center" spacing={0}>
                            <Grid item>
                                {/* Dataset options button */}
                                <IconButton
                                    color="secondary"
                                    onClick={handleDatasetOptionsClick}
                                >
                                    <MoreVertIcon />
                                </IconButton>

                                {/* Dataset options */}
                                <Menu
                                    anchorEl={datasetOptionsAnchorElement}
                                    open={Boolean(datasetOptionsAnchorElement)}
                                    onClose={handleDatasetOptionsClose}
                                >
                                    {/* View details */}
                                    {props.showViewDetails && (
                                        <MenuItem
                                            onClick={handleDatasetViewDetails}
                                        >
                                            {t("label.view-details")}
                                        </MenuItem>
                                    )}

                                    {/* Edit dataset */}
                                    <MenuItem onClick={handleDatasetEdit}>
                                        {t("label.edit-entity", {
                                            entity: t("label.dataset"),
                                        })}
                                    </MenuItem>

                                    {/* Delete dataset */}
                                    <MenuItem onClick={handleDatasetDelete}>
                                        {t("label.delete-entity", {
                                            entity: t("label.dataset"),
                                        })}
                                    </MenuItem>
                                </Menu>
                            </Grid>
                        </Grid>
                    }
                    title={
                        <>
                            {/* Dataset name */}
                            {props.showViewDetails && (
                                <Link onClick={handleDatasetViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiDataset.name}
                                    />
                                </Link>
                            )}

                            {/* Summary */}
                            {!props.showViewDetails && t("label.summary")}
                        </>
                    }
                    titleTypographyProps={{ variant: "h6" }}
                />
            )}

            <CardContent>
                {props.uiDataset && (
                    <Grid container>
                        {/* Datasource */}
                        <Grid item md={4} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.datasource")}
                                searchWords={props.searchWords}
                                values={[props.uiDataset.datasourceName]}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.uiDataset && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};

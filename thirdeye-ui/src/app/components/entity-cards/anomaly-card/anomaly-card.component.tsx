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
import classnames from "classnames";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { getAnomaliesAnomalyPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { AnomalyCardProps } from "./anomaly-card.interfaces";
import { useAnomalyCardStyles } from "./anomaly-card.styles";

export const AnomalyCard: FunctionComponent<AnomalyCardProps> = (
    props: AnomalyCardProps
) => {
    const anomalyCardClasses = useAnomalyCardStyles();
    const [anomalyOptionsAnchorElement, setAnomalyOptionsAnchorElement] =
        useState<HTMLElement | null>();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { t } = useTranslation();

    const handleAnomalyOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setAnomalyOptionsAnchorElement(event.currentTarget);
    };

    const handleAnomalyOptionsClose = (): void => {
        setAnomalyOptionsAnchorElement(null);
    };

    const handleAnomalyViewDetails = (): void => {
        if (!props.uiAnomaly) {
            return;
        }

        navigate(getAnomaliesAnomalyPath(props.uiAnomaly.id));
        handleAnomalyOptionsClose();
    };

    const handleAnomalyDelete = (): void => {
        if (!props.uiAnomaly) {
            return;
        }

        props.onDelete && props.onDelete(props.uiAnomaly);
        handleAnomalyOptionsClose();
    };

    if (props.isLoading) {
        return (
            <PageContentsCardV1 className={props.className}>
                <SkeletonV1 height={150} variant="rect" />
            </PageContentsCardV1>
        );
    }

    return (
        <Card className={props.className} variant="outlined">
            {props.uiAnomaly && (
                <CardHeader
                    action={
                        props.showViewDetails ? (
                            <>
                                {/* Anomaly options button */}
                                <IconButton
                                    color="secondary"
                                    onClick={handleAnomalyOptionsClick}
                                >
                                    <MoreVertIcon />
                                </IconButton>

                                {/* Anomaly options */}
                                <Menu
                                    anchorEl={anomalyOptionsAnchorElement}
                                    open={Boolean(anomalyOptionsAnchorElement)}
                                    onClose={handleAnomalyOptionsClose}
                                >
                                    {/* View details */}
                                    <MenuItem
                                        onClick={handleAnomalyViewDetails}
                                    >
                                        {t("label.view-details")}
                                    </MenuItem>
                                    {/* Delete anomaly */}
                                    <MenuItem onClick={handleAnomalyDelete}>
                                        {t("label.delete-entity", {
                                            entity: t("label.anomaly"),
                                        })}
                                    </MenuItem>
                                </Menu>
                            </>
                        ) : null
                    }
                    title={
                        <>
                            {/* Anomaly name */}
                            {props.showViewDetails && (
                                <Link onClick={handleAnomalyViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiAnomaly.name}
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
                {props.uiAnomaly && (
                    <Grid container>
                        {/* Start */}
                        <Grid item>
                            <NameValueDisplayCard<string>
                                name={t("label.start")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.startTime]}
                            />
                        </Grid>

                        {/* End */}
                        <Grid item>
                            <NameValueDisplayCard<string>
                                name={t("label.end")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.endTime]}
                            />
                        </Grid>

                        {/* Duration */}
                        <Grid item>
                            <NameValueDisplayCard<string>
                                name={t("label.duration")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.duration]}
                            />
                        </Grid>

                        {/* Current/Predicted */}
                        <Grid item>
                            <NameValueDisplayCard<string>
                                name={`${t("label.current")}${t(
                                    "label.pair-separator"
                                )}${t("label.predicted")}`}
                                searchWords={props.searchWords}
                                values={[
                                    `${props.uiAnomaly.current}${t(
                                        "label.pair-separator"
                                    )}${props.uiAnomaly.predicted}`,
                                ]}
                            />
                        </Grid>

                        {/* Deviation */}
                        <Grid item>
                            <NameValueDisplayCard<string>
                                name={t("label.deviation")}
                                searchWords={props.searchWords}
                                valueClassName={classnames({
                                    [anomalyCardClasses.deviation]:
                                        props.uiAnomaly.negativeDeviation,
                                })}
                                values={[props.uiAnomaly.deviation]}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.uiAnomaly && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};

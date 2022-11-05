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
import { Box, Button, Grid, Link, Typography } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import FindInPageIcon from "@material-ui/icons/FindInPage";
import PlayCircleFilledIcon from "@material-ui/icons/PlayCircleFilled";
import SchoolIcon from "@material-ui/icons/School";
import WifiTetheringIcon from "@material-ui/icons/WifiTethering";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { RecommendedDocumentationProps } from "./recommended-documentation.interfaces";

export const RecommendedDocumentation: FunctionComponent<
    RecommendedDocumentationProps
> = ({ onHideDocumentationClick }) => {
    const { t } = useTranslation();

    return (
        <>
            <Box paddingBottom={2} paddingTop={1}>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item>
                        <Typography variant="h6">
                            {t("label.recommended-documentation")}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Button
                            variant="text"
                            onClick={onHideDocumentationClick}
                        >
                            <Grid container alignItems="center">
                                <Grid item>{t("label.dont-show-again")}</Grid>
                                <Grid item>
                                    <CloseIcon
                                        color="secondary"
                                        fontSize="small"
                                    />
                                </Grid>
                            </Grid>
                        </Button>
                    </Grid>
                </Grid>
            </Box>
            <Grid container alignItems="stretch">
                <Grid item sm={3} xs={12}>
                    <PageContentsCardV1 fullHeight>
                        <Link href={t("link.getting-started")} target="_blank">
                            <Grid container alignItems="center" wrap="nowrap">
                                <Grid item>
                                    <SchoolIcon fontSize="large" />
                                </Grid>
                                <Grid item>
                                    <Typography
                                        color="textPrimary"
                                        variant="subtitle1"
                                    >
                                        <Grid item>
                                            {t("label.getting-started")}
                                        </Grid>
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Link>
                    </PageContentsCardV1>
                </Grid>
                <Grid item sm={3} xs={12}>
                    <PageContentsCardV1 fullHeight>
                        <Link href={t("link.create-an-alert")} target="_blank">
                            <Grid container alignItems="center" wrap="nowrap">
                                <Grid item>
                                    <WifiTetheringIcon
                                        color="primary"
                                        fontSize="large"
                                    />
                                </Grid>
                                <Grid item>
                                    <Typography
                                        color="textPrimary"
                                        variant="subtitle1"
                                    >
                                        {t("label.create-an-entity", {
                                            entity: t("label.alert"),
                                        })}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Link>
                    </PageContentsCardV1>
                </Grid>
                <Grid item sm={3} xs={12}>
                    <PageContentsCardV1 fullHeight>
                        <Link
                            href={t("link.how-to-use-te-video")}
                            target="_blank"
                        >
                            <Grid container alignItems="center" wrap="nowrap">
                                <Grid item>
                                    <FindInPageIcon
                                        color="primary"
                                        fontSize="large"
                                    />
                                </Grid>
                                <Grid item>
                                    <Typography
                                        color="textPrimary"
                                        variant="subtitle1"
                                    >
                                        {t("label.how-to-investigate-anomaly")}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Link>
                    </PageContentsCardV1>
                </Grid>
                <Grid item sm={3} xs={12}>
                    <PageContentsCardV1 fullHeight>
                        <Link
                            href={t("link.how-to-use-te-video")}
                            target="_blank"
                        >
                            <Grid container alignItems="center" wrap="nowrap">
                                <Grid item>
                                    <PlayCircleFilledIcon
                                        color="primary"
                                        fontSize="large"
                                    />
                                </Grid>
                                <Grid item>
                                    <Typography
                                        color="textPrimary"
                                        variant="subtitle1"
                                    >
                                        {t("label.learn-how-to-use-te")}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Link>
                    </PageContentsCardV1>
                </Grid>
            </Grid>
        </>
    );
};

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
import { Box, Button, Grid, Link, Typography } from "@material-ui/core";
import { ArrowForward } from "@material-ui/icons";
import DvrIcon from "@material-ui/icons/Dvr";
import PlayCircleFilledIcon from "@material-ui/icons/PlayCircleFilled";
import WidgetsIcon from "@material-ui/icons/Widgets";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import CreateAlertImage from "../../../../assets/images/create-alert.png";
import GettingStartedImage from "../../../../assets/images/getting-started.png";
import InvestigateAnomaliesImage from "../../../../assets/images/investigate-anomalies.png";
import { THIRDEYE_DOC_LINK } from "../../../utils/constants/constants.util";
import { useLatestSubscriptionGroupsStyles } from "../latest-subscription-groups/latest-subscription-groups.styles";
import { useRecommendedDocumentationV2Styles } from "./recommended-documentation-v2.styles";

export const RecommendedDocumentationV2: FunctionComponent = () => {
    const { t } = useTranslation();
    const styles = useRecommendedDocumentationV2Styles();
    const linkStyles = useLatestSubscriptionGroupsStyles();

    const items = useMemo(
        () => [
            {
                href: t("url.getting-started"),
                label: t("label.getting-started"),
                image: GettingStartedImage,
            },
            {
                href: t("url.create-an-alert"),
                label: t("label.how-to-create-an-alert"),
                image: CreateAlertImage,
            },
            {
                href: t("url.how-to-use-te-video"),
                label: t("label.how-to-investigate-anomalies"),
                image: InvestigateAnomaliesImage,
            },
            {
                icon: <PlayCircleFilledIcon fontSize="large" />,
                href: t("url.how-to-use-te-video"),
                label: t("label.learn-how-to-use-te"),
            },
            {
                icon: <WidgetsIcon fontSize="large" />,
                href: t("url.te-recipes"),
                label: t("message.te-recipes"),
            },
            {
                icon: <DvrIcon fontSize="large" />,
                href: t("url.alert-config-examples"),
                label: t("message.alert-configuration-examples"),
            },
        ],
        []
    );

    return (
        <>
            <Box paddingBottom={2} paddingTop={1}>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item>
                        <Typography variant="h5">
                            {t("label.thirdeye-101")}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Link
                            className={linkStyles.allAlertsLink}
                            component={Link}
                            href={THIRDEYE_DOC_LINK}
                            target="_blank"
                        >
                            <Grid container alignItems="center">
                                <Grid item>
                                    {t("label.view-all-entities", {
                                        entity: t("label.documentation"),
                                    })}
                                </Grid>
                                <Grid item>
                                    <ArrowForward
                                        color="primary"
                                        fontSize="small"
                                    />
                                </Grid>
                            </Grid>
                        </Link>
                    </Grid>
                </Grid>
            </Box>
            <Grid
                container
                className={styles.gridContainer}
                direction="row"
                spacing={4}
            >
                {items.map(({ href, label, icon, image }) => (
                    <Grid item key={label} sm={4} xs={12}>
                        <Link
                            className={
                                image ? styles.imageCard : styles.nonImageCard
                            }
                            href={href}
                            target="_blank"
                        >
                            {image ? (
                                <img
                                    alt={label}
                                    className={styles.cardImage}
                                    src={image}
                                />
                            ) : null}

                            <Grid
                                container
                                alignItems="center"
                                wrap="nowrap"
                                {...(image
                                    ? {
                                          className:
                                              styles.imageCardTextContainer,
                                      }
                                    : {})}
                            >
                                {icon ? (
                                    <Grid item className={styles.cardIcon}>
                                        {icon}
                                    </Grid>
                                ) : null}
                                <Grid item>
                                    <Typography
                                        className={styles.cardText}
                                        variant="subtitle1"
                                    >
                                        <Grid item>{label}</Grid>
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Link>
                    </Grid>
                ))}
            </Grid>
        </>
    );
};

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
import { Box } from "@material-ui/core";
import i18next from "i18next";
import { capitalize } from "lodash";
import React from "react";
import { Trans } from "react-i18next";
import { LinkButtonV1, LinkV1 } from "../../platform/components";
import { IframeVideoPlayerContainer } from "../iframe-video-player-container/iframe-video-player-container.component";
import { HelpDrawerV1Card } from "./help-drawer-v1.interfaces";

export const alertsBasicHelpCards: HelpDrawerV1Card[] = [
    {
        title: i18next.t("message.how-to-action-entity", {
            action: i18next.t("label.create"),
            entity: i18next.t("label.alerts"),
        }),
        body: (
            <>
                <IframeVideoPlayerContainer>
                    <iframe
                        allowFullScreen
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        frameBorder="0"
                        src={i18next.t("url.startree-te-demo-video-embed")}
                    />
                </IframeVideoPlayerContainer>
                <LinkButtonV1
                    externalLink
                    color="primary"
                    href={i18next.t("url.startree-te-demo-video-link")}
                    target="_blank"
                    variant="outlined"
                >
                    <Box component="span" display="flex" mr={1}>
                        <Icon icon="material-symbols:slow-motion-video-sharp" />
                    </Box>
                    <Box component="span">
                        {i18next.t("label.view-entity", {
                            entity: i18next.t("label.video"),
                        })}
                    </Box>
                </LinkButtonV1>
            </>
        ),
    },
    {
        title: i18next.t("message.what-are-entity", {
            entity: i18next.t("label.alerts"),
        }),
        body: (
            <>
                {i18next.t("message.alerts-helper-description")}{" "}
                <LinkV1
                    externalLink
                    href={i18next.t("url.create-an-alert")}
                    target="_blank"
                    variant="body2"
                >
                    {capitalize(
                        i18next.t("message.learn-more-about-entity", {
                            entity: i18next.t("label.alerts"),
                        })
                    )}
                </LinkV1>
            </>
        ),
    },
    {
        title: i18next.t("message.what-are-entity", {
            entity: i18next.t("label.alert-templates"),
        }),
        body: (
            <>
                {i18next.t("message.alert-templates-helper-description")}{" "}
                <LinkV1
                    externalLink
                    href={i18next.t("url.alert-templates-docs")}
                    target="_blank"
                    variant="body2"
                >
                    {capitalize(
                        i18next.t("message.learn-more-about-entity", {
                            entity: i18next.t("label.alert-templates"),
                        })
                    )}
                </LinkV1>
            </>
        ),
    },
];

export const anomaliesBasicHelpCards: HelpDrawerV1Card[] = [
    {
        title: i18next.t("message.how-to-action-entity", {
            action: i18next.t("label.view"),
            entity: i18next.t("label.anomalies"),
        }),
        body: (
            <>
                <IframeVideoPlayerContainer>
                    <iframe
                        allowFullScreen
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        frameBorder="0"
                        src={i18next.t("url.startree-te-demo-video-embed")}
                    />
                </IframeVideoPlayerContainer>
                <LinkButtonV1
                    externalLink
                    color="primary"
                    href={i18next.t("url.startree-te-demo-video-link")}
                    target="_blank"
                    variant="outlined"
                >
                    <Box component="span" display="flex" mr={1}>
                        <Icon icon="material-symbols:slow-motion-video-sharp" />
                    </Box>
                    <Box component="span">
                        {i18next.t("label.view-entity", {
                            entity: i18next.t("label.video"),
                        })}
                    </Box>
                </LinkButtonV1>
            </>
        ),
    },
    {
        title: i18next.t("message.what-is-an-entity", {
            entity: i18next.t("label.anomaly"),
        }),
        body: (
            <>
                <Trans i18nKey="message.anomaly-helper-description" />{" "}
                <LinkV1
                    externalLink
                    href={i18next.t("url.how-to-investigate-anomaly")}
                    target="_blank"
                    variant="body2"
                >
                    {capitalize(
                        i18next.t("message.learn-more-about-entity", {
                            entity: i18next.t("label.anomalies"),
                        })
                    )}
                </LinkV1>
            </>
        ),
    },
];

export const anomalyCreateBasicHelpCards: HelpDrawerV1Card[] = [
    {
        title: i18next.t("message.how-to-action-entity", {
            action: i18next.t("label.report-missed"),
            entity: i18next.t("label.anomalies"),
        }),
        body: (
            <>
                <IframeVideoPlayerContainer>
                    <iframe
                        allowFullScreen
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        frameBorder="0"
                        src={i18next.t("url.report-missed-anomaly-video-embed")}
                    />
                </IframeVideoPlayerContainer>
                <LinkButtonV1
                    externalLink
                    color="primary"
                    href={i18next.t("url.report-missed-anomaly-video-link")}
                    target="_blank"
                    variant="outlined"
                >
                    <Box component="span" display="flex" mr={1}>
                        <Icon icon="material-symbols:slow-motion-video-sharp" />
                    </Box>
                    <Box component="span">
                        {i18next.t("label.view-entity", {
                            entity: i18next.t("label.video"),
                        })}
                    </Box>
                </LinkButtonV1>
            </>
        ),
    },
    {
        title: i18next.t("message.what-is-an-entity", {
            entity: i18next.t("label.anomaly"),
        }),
        body: (
            <>
                <Trans i18nKey="message.anomaly-helper-description" />{" "}
                <LinkV1
                    externalLink
                    href={i18next.t("url.how-to-investigate-anomaly")}
                    target="_blank"
                    variant="body2"
                >
                    {capitalize(
                        i18next.t("message.learn-more-about-entity", {
                            entity: i18next.t("label.anomalies"),
                        })
                    )}
                </LinkV1>
            </>
        ),
    },
];

export const anomaliesInvestigateBasicHelpCards: HelpDrawerV1Card[] = [
    {
        title: i18next.t("message.how-to-action-entity", {
            action: i18next.t("label.investigate"),
            entity: i18next.t("label.anomalies"),
        }),
        body: (
            <>
                <IframeVideoPlayerContainer>
                    <iframe
                        allowFullScreen
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        frameBorder="0"
                        src={i18next.t("url.how-to-use-te-video")}
                    />
                </IframeVideoPlayerContainer>
                <LinkButtonV1
                    externalLink
                    color="primary"
                    href={i18next.t("url.how-to-use-te-video")}
                    target="_blank"
                    variant="outlined"
                >
                    <Box component="span" display="flex" mr={1}>
                        <Icon icon="material-symbols:slow-motion-video-sharp" />
                    </Box>
                    <Box component="span">
                        {i18next.t("label.view-entity", {
                            entity: i18next.t("label.video"),
                        })}
                    </Box>
                </LinkButtonV1>
            </>
        ),
    },
    {
        title: i18next.t("message.what-is-an-entity", {
            entity: i18next.t("label.anomaly-investigation"),
        }),
        body: (
            <>
                {i18next.t("message.investigate-helper-description")}{" "}
                <LinkV1
                    externalLink
                    href={i18next.t("url.how-to-investigate-anomaly")}
                    target="_blank"
                    variant="body2"
                >
                    {capitalize(
                        i18next.t("message.learn-more-about-entity", {
                            entity: i18next.t("label.anomaly-investigation"),
                        })
                    )}
                </LinkV1>
            </>
        ),
    },
    {
        title: i18next.t("label.learn-more"),
        body: (
            <>
                <IframeVideoPlayerContainer>
                    <iframe
                        allowFullScreen
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        frameBorder="0"
                        src={i18next.t("url.investigate-anomalies-video-embed")}
                    />
                </IframeVideoPlayerContainer>
                <LinkButtonV1
                    externalLink
                    color="primary"
                    href={i18next.t("url.investigate-anomalies-video-link")}
                    target="_blank"
                    variant="outlined"
                >
                    <Box component="span" display="flex" mr={1}>
                        <Icon icon="material-symbols:slow-motion-video-sharp" />
                    </Box>
                    <Box component="span">
                        {i18next.t("label.view-entity", {
                            entity: i18next.t("label.video"),
                        })}
                    </Box>
                </LinkButtonV1>
            </>
        ),
    },
];

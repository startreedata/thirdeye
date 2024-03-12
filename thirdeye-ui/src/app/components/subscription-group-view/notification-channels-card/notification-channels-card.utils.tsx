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
import Box from "@material-ui/core/Box";
import Checkbox from "@material-ui/core/Checkbox";
import React from "react";
import { TFunction } from "react-i18next";
import { lightV1 } from "../../../platform/utils";
import {
    NotificationSpec,
    SpecType,
} from "../../../rest/dto/subscription-group.interfaces";
import { SubscriptionGroupViewCardProps } from "../../entity-cards/subscription-group-view-card/subscription-group-view-card.interfaces";

export const subscriptionGroupChannelHeaderMap = {
    [SpecType.Webhook]: "label.webhook",
    [SpecType.Slack]: "label.slack",
    [SpecType.EmailSendgrid]: "label.email",
    [SpecType.PagerDuty]: "label.pager-duty",
} as const;

export const subscriptionGroupChannelIconsMap = {
    [SpecType.Webhook]: "material-symbols:webhook-rounded",
    [SpecType.Slack]: "mdi:slack",
    [SpecType.EmailSendgrid]: "ic:outline-email",
    [SpecType.PagerDuty]: "simple-icons:pagerduty",
} as const;

export const getCardProps = (
    channel: NotificationSpec,
    t: TFunction
): SubscriptionGroupViewCardProps => {
    const cardProps: SubscriptionGroupViewCardProps = {
        header: (
            <Box alignItems="center" display="flex" gridGap={8}>
                <Icon
                    color={lightV1.palette.primary.dark}
                    fontSize={32}
                    icon={subscriptionGroupChannelIconsMap[channel.type]}
                />
                {t(
                    subscriptionGroupChannelHeaderMap[channel.type] ||
                        "label.email"
                )}
            </Box>
        ),
        rows: [],
    };

    switch (channel.type) {
        case SpecType.Webhook:
            cardProps.rows.push({
                label: t("label.url"),
                value: channel.params.url,
            });

            break;
        case SpecType.PagerDuty:
            cardProps.rows.push({
                label: t("label.integration-key"),
                value: channel.params.eventsIntegrationKey,
            });

            break;

        case SpecType.Slack:
            cardProps.rows.push({
                label: t("label.url"),
                value: channel.params.webhookUrl,
            });

            cardProps.rows.push({
                label: t("message.notify-when-the-anomaly-period-ends"),
                value: (
                    <Box clone ml={-1.25}>
                        <Checkbox
                            disabled
                            checked={channel.params.notifyResolvedAnomalies}
                            color="primary"
                            size="small"
                        />
                    </Box>
                ),
            });

            break;

        case SpecType.EmailSendgrid:
            cardProps.rows.push(
                {
                    label: t("label.sendgrid-api-key"),
                    value: channel.params.apiKey,
                },
                {
                    label: t("label.from"),
                    value: channel.params.emailRecipients.from,
                },
                {
                    label: t("label.to"),
                    value: channel.params.emailRecipients.to.join(","),
                }
            );

            break;
        // This case below should NEVER happen, but adding here in case
        // the back-end adds a new notification channel type without the
        // UI getting updated to support the same
        default:
            cardProps.rows.push({
                label: t("label.configuration"),
                value: JSON.stringify(channel),
            });

            break;
    }

    return cardProps;
};

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
import {
    NotificationSpec,
    PagerDutySpec,
    SendgridEmailSpec,
    SlackSpec,
    SpecType,
    WebhookSpec,
} from "../../../../../rest/dto/subscription-group.interfaces";
import {
    subscriptionGroupChannelHeaderMap,
    subscriptionGroupChannelIconsMap,
} from "../../../../subscription-group-view/notification-channels-card/notification-channels-card.utils";
import { SendgridEmailReview } from "../groups-renderer/sendgrid-email-review/sendgrid-email-review.component";
import { SpecUIConfig } from "./groups-editor.interfaces";
import { PagerDuty } from "./pager-duty/pager-duty.component";
import { SendgridEmail } from "./sendgrid-email/sendgrid-email.component";
import { Slack } from "./slack/slack.component";
import { Webhook } from "./webhook/webhook.component";

export const availableSpecTypes: SpecUIConfig[] = [
    {
        id: SpecType.EmailSendgrid,
        internationalizationString:
            subscriptionGroupChannelHeaderMap[SpecType.EmailSendgrid],
        icon: subscriptionGroupChannelIconsMap[SpecType.EmailSendgrid],
        formComponent: SendgridEmail,
        reviewComponent: SendgridEmailReview,
        viewComponent: SendgridEmailReview,
        validate: (spec: NotificationSpec): boolean => {
            const specTyped = spec as SendgridEmailSpec;

            return (
                specTyped.params.apiKey !== "" &&
                specTyped.params.emailRecipients.from !== ""
            );
        },
    },
    {
        id: SpecType.Slack,
        internationalizationString:
            subscriptionGroupChannelHeaderMap[SpecType.Slack],
        icon: subscriptionGroupChannelIconsMap[SpecType.Slack],
        formComponent: Slack,
        reviewComponent: (props) => props.configuration.params.webhookUrl,
        viewComponent: Slack,
        validate: (spec: NotificationSpec): boolean => {
            const specTyped = spec as SlackSpec;

            return specTyped.params.webhookUrl !== "";
        },
    },
    {
        id: SpecType.Webhook,
        internationalizationString:
            subscriptionGroupChannelHeaderMap[SpecType.Webhook],
        icon: subscriptionGroupChannelIconsMap[SpecType.Webhook],
        formComponent: Webhook,
        reviewComponent: (props) => props.configuration.params.url,
        viewComponent: Webhook,
        validate: (spec: NotificationSpec): boolean => {
            const specTyped = spec as WebhookSpec;

            return specTyped.params.url !== "";
        },
    },
    {
        id: SpecType.PagerDuty,
        internationalizationString:
            subscriptionGroupChannelHeaderMap[SpecType.PagerDuty],
        icon: subscriptionGroupChannelIconsMap[SpecType.PagerDuty],
        formComponent: PagerDuty,
        reviewComponent: (props) => props.configuration.params.url,
        viewComponent: PagerDuty,
        validate: (spec: NotificationSpec): boolean => {
            const specTyped = spec as PagerDutySpec;

            return specTyped.params.eventsIntegrationKey !== "";
        },
    },
];

export const specTypeToUIConfig: { [key: string]: SpecUIConfig } = {};

availableSpecTypes.forEach((item) => {
    specTypeToUIConfig[item.id] = item;
});

export const generateEmptyEmailSendGridConfiguration =
    (): SendgridEmailSpec => {
        return {
            type: SpecType.EmailSendgrid,
            params: {
                apiKey: "${SENDGRID_API_KEY}",
                emailRecipients: {
                    from: "thirdeye-alerts@startree.ai",
                    to: [],
                },
            },
        };
    };

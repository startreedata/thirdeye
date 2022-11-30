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
    NotificationSpec,
    SendgridEmailSpec,
    SlackSpec,
    SpecType,
    WebhookSpec,
} from "../../../rest/dto/subscription-group.interfaces";
import { SendgridEmailReview } from "../subscription-group-renderer/sendgrid-email-review/sendgrid-email-review.component";
import { SpecUIConfig } from "./groups-editor.interfaces";
import { SendgridEmail } from "./sendgrid-email/sendgrid-email.component";
import { Slack } from "./slack/slack.component";
import { Webhook } from "./webhook/webhook.component";

export const availableSpecTypes: SpecUIConfig[] = [
    {
        id: SpecType.EmailSendgrid,
        internationalizationString: "label.email",
        icon: "ic:twotone-email",
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
        internationalizationString: "label.slack",
        icon: "logos:slack-icon",
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
        internationalizationString: "label.webhook",
        icon: "logos:webhooks",
        formComponent: Webhook,
        reviewComponent: (props) => props.configuration.params.url,
        viewComponent: Webhook,
        validate: (spec: NotificationSpec): boolean => {
            const specTyped = spec as WebhookSpec;

            return specTyped.params.url !== "";
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

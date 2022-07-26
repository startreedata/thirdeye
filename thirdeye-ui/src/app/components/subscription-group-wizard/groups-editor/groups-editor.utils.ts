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
import { SpecType } from "../../../rest/dto/subscription-group.interfaces";
import { SendgridEmailReview } from "../subscription-group-renderer/sendgrid-email-review/sendgrid-email-review.component";
import { SpecUIConfig } from "./groups-editor.interfaces";
import { SendgridEmail } from "./sendgrid-email/sendgrid-email.component";
import { Slack } from "./slack/slack.component";
import { Webhook } from "./webhook/webhook.component";

export const availableSpecTypes: SpecUIConfig[] = [
    {
        id: SpecType.EmailSendgrid,
        internationalizationString: "label.email",
        icon: "carbon:email",
        formComponent: SendgridEmail,
        reviewComponent: SendgridEmailReview,
    },
    {
        id: SpecType.Slack,
        internationalizationString: "label.slack",
        icon: "logos:slack-icon",
        formComponent: Slack,
        reviewComponent: (props) => props.configuration.params.webhookUrl,
    },
    {
        id: SpecType.Webhook,
        internationalizationString: "label.webhook",
        icon: "logos:webhooks",
        formComponent: Webhook,
        reviewComponent: (props) => props.configuration.params.url,
    },
];

export const specTypeToUIConfig: { [key: string]: SpecUIConfig } = {};

availableSpecTypes.forEach((item) => {
    specTypeToUIConfig[item.id] = item;
});

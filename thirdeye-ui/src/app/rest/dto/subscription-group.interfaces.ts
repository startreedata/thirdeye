// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Alert } from "./alert.interfaces";
import { Application } from "./application.interfaces";
import { User } from "./user.interfaces";

export interface SubscriptionGroup {
    id: number;
    name: string;
    cron: string;
    application: Application;
    alerts: Alert[];
    created: number;
    updated: number;
    owner: User;
    notificationSchemes: NotificationSchemes;
    specs: NotificationSpec[];
}

export enum SpecType {
    Slack = "slack",
    Webhook = "webhook",
    EmailSendgrid = "email-sendgrid",
}

export interface SpecConfiguration {
    type: string | SpecType;
    params: {
        [key: string]: unknown;
    };
}

export interface SlackSpec extends SpecConfiguration {
    type: SpecType.Slack;
    params: {
        webhookUrl: string;
    };
}

export interface WebhookSpec extends SpecConfiguration {
    type: SpecType.Webhook;
    params: {
        url: string;
    };
}

export interface SendgridEmailSpec extends SpecConfiguration {
    type: SpecType.EmailSendgrid;
    params: {
        apiKey: string;
        emailRecipients: {
            from: string;
            to: string[];
        };
    };
}

export type NotificationSpec = SlackSpec | WebhookSpec | SendgridEmailSpec;

export interface NotificationSchemes {
    email: EmailScheme;
}

export interface EmailScheme {
    from: string;
    to: string[];
    cc: string[];
    bcc: string[];
}

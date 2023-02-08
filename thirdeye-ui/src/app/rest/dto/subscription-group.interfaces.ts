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
import type { Alert } from "./alert.interfaces";
import type { Application } from "./application.interfaces";
import type { User } from "./user.interfaces";

export interface AlertAssociation {
    alert: { id: number };
    enumerationItem?: { id: number };
}

export interface SubscriptionGroup {
    id: number;
    name: string;
    cron: string;
    application: Application;
    alerts?: Alert[]; // deprecated
    alertAssociations?: AlertAssociation[];
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
    params: Record<string, unknown>;
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

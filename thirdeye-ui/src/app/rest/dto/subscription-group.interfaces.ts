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
    notificationSchemes?: NotificationSchemes;
    specs: NotificationSpec[];
    notifyHistoricalAnomalies?: boolean; // optional as its only coming when this property is set
    minimumAnomalyLength?: string | null;
}

export enum SpecType {
    Slack = "slack",
    Webhook = "webhook",
    EmailSendgrid = "email-sendgrid",
    PagerDuty = "pagerduty",
}

export interface SpecConfiguration {
    type: string | SpecType;
    params: Record<string, unknown>;
}

export interface SlackSpec extends SpecConfiguration {
    type: SpecType.Slack;
    params: {
        webhookUrl: string;
        notifyResolvedAnomalies: boolean;
        sendOneMessagePerAnomaly: boolean;
        textConfiguration: {
            owner: string;
            mentionMemberIds: string[];
        };
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

export interface PagerDutySpec extends SpecConfiguration {
    type: SpecType.PagerDuty;
    params: {
        eventsIntegrationKey: string;
    };
}

export type NotificationSpec =
    | SlackSpec
    | WebhookSpec
    | SendgridEmailSpec
    | PagerDutySpec;

export interface NotificationSchemes {
    email: EmailScheme;
}

export interface EmailScheme {
    from: string;
    to: string[];
    cc: string[];
    bcc: string[];
}

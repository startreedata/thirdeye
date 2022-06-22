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
}

export interface NotificationSchemes {
    email: EmailScheme;
}

export interface EmailScheme {
    from: string;
    to: string[];
    cc: string[];
    bcc: string[];
}

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

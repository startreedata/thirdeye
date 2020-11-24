import { Alert } from "./alert.interfaces";
import { Application } from "./application.interfaces";
import { User } from "./user.interfaces";

export interface SubscriptionGroup {
    id: number;
    name: string;
    application: Application;
    alerts: Alert[];
    created: number;
    upnumberd: number;
    owner: User;
    emailSettings: EmailSettings;
}

export interface EmailSettings {
    from: string;
    to: string[];
    cc: string[];
    bcc: string[];
}

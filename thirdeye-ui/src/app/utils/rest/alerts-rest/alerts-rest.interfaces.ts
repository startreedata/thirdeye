import { User } from "../authentication-rest/authentication-rest.interfaces";

export interface Alert {
    id: number;
    name: string;
    description: string;
    cron: string;
    lastTimestamp: Date;
    active: boolean;
    created: Date;
    updated: Date;
    User: User;
}

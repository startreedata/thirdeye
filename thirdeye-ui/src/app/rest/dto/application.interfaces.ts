import { User } from "./user.interfaces";

export interface Application {
    id: number;
    name: string;
    created: Date;
    owner: User;
}

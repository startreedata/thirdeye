import { User } from "./user.interfaces";

export interface Application {
    id: number;
    name: string;
    created: number;
    owner: User;
}

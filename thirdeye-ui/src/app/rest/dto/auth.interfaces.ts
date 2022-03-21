import { User } from "./user.interfaces";

export interface Auth {
    user: User;
    accessToken: string;
}

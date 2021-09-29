import { Auth } from "../../dto/auth.interfaces";
import { ActionHook } from "../actions.interfaces";

export interface Login extends ActionHook {
    auth: Auth | null;
    login: () => Promise<void>;
}

export interface Logout extends ActionHook {
    logout: () => Promise<void>;
}

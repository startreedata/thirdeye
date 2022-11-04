import { ActionHook } from "../actions.interfaces";
import { InfoV1 } from "../dto/info.interfaces";

export interface GetInfoV1 extends ActionHook {
    infoV1: InfoV1 | null;
    getInfoV1: () => Promise<void>;
}

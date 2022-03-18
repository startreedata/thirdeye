import { ActionHook } from "../actions.interfaces";
import { AppConfiguration } from "../dto/app-config.interface";

export interface GetAppConfiguration extends ActionHook {
    appConfig: AppConfiguration | null;
    getAppConfiguration: () => Promise<AppConfiguration | undefined>;
}

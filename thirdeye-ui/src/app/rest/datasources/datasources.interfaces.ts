import { ActionHook } from "../actions.interfaces";
import { Datasource } from "../dto/datasource.interfaces";

export interface GetStatusResponse {
    code?: string;
    list?: {
        code: string;
        msg: string;
    }[];
}

export interface GetDatasourceStatus extends ActionHook {
    healthStatus: GetStatusResponse | null;
    getDatasourceStatus: (
        datasourceName: string
    ) => Promise<GetStatusResponse | undefined>;
}

export interface GetDatasources extends ActionHook {
    datasources: Datasource[] | null;
    getDatasources: () => Promise<Datasource[] | undefined>;
}

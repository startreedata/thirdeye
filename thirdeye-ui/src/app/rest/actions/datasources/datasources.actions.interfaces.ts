import { Datasource } from "../../dto/datasource.interfaces";
import { ActionHook } from "../actions.interfaces";

export interface FetchDatasource extends ActionHook {
    datasource: Datasource | null;
    fetchDatasource: (id: number) => Promise<void>;
}

export interface FetchAllDatasource extends ActionHook {
    datasources: Datasource[] | null;
    fetchAllDatasource: () => Promise<void>;
}

export interface CreateDatasource extends ActionHook {
    createDatasource: (datasource: Datasource) => Promise<void>;
}

export interface CreateDatasources extends ActionHook {
    createDatasources: (datasources: Datasource[]) => Promise<void>;
}

export interface UpdateDatasource extends ActionHook {
    updateDatasource: (datasource: Datasource) => Promise<void>;
}

export interface UpdateDatasources extends ActionHook {
    updateDatasources: (datasources: Datasource[]) => Promise<void>;
}

export interface DeleteDatasource extends ActionHook {
    deleteDatasource: (id: number) => Promise<void>;
}

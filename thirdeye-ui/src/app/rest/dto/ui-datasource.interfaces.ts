import { Datasource } from "./datasource.interfaces";

export interface UiDatasource {
    id: number;
    name: string;
    type: string;
    datasource: Datasource | null;
}

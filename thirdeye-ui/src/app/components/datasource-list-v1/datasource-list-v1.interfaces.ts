import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";

export interface DatasourceListV1Props {
    datasources: UiDatasource[] | null;
    onDelete?: (uiDatasource: UiDatasource) => void;
}

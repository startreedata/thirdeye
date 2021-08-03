import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";

export interface DatasourceListProps {
    hideSearchBar?: boolean;
    datasources: UiDatasource[] | null;
    onChange?: (uiDatasource: UiDatasource) => void;
    onDelete?: (uiDatasource: UiDatasource) => void;
}

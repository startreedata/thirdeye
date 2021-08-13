import { UiDatasource } from "../../../rest/dto/ui-datasource.interfaces";

export interface DatasourceCardProps {
    uiDatasource: UiDatasource | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (uiDatasource: UiDatasource) => void;
}

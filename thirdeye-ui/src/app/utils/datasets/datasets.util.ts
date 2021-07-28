import i18n from "i18next";
import { cloneDeep, isEmpty } from "lodash";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import {
    UiDataset,
    UiDatasetDatasource,
} from "../../rest/dto/ui-dataset.interfaces";
import { formatNumber } from "../number/number.util";
import { deepSearchStringProperty } from "../search/search.util";

export const createEmptyDataset = (): Dataset => {
    return {
        name: "",
        datasources: [] as Datasource[],
    } as Dataset;
};

export const createEmptyUiDataset = (): UiDataset => {
    return {
        id: -1,
        name: i18n.t("label.no-data-marker"),
        datasources: [],
        datasourceCount: formatNumber(0),
        dataset: null,
    };
};

export const createEmptyUiDatasetDatasource = (): UiDatasetDatasource => {
    return {
        id: -1,
        name: i18n.t("label.no-data-marker"),
    };
};

export const getUiDataset = (
    dataset: Dataset,
    datasources: Datasource[]
): UiDataset => {
    if (!dataset) {
        return createEmptyUiDataset();
    }

    // Map datasources to dataset ids
    const datasourcesToDatasetIdsMap = mapDatasourcesToDatasetIds(
        [dataset],
        datasources
    );

    return getUiDatasetInternal(dataset, datasourcesToDatasetIdsMap);
};

export const getUiDatasets = (
    datasets: Dataset[],
    datasources: Datasource[]
): UiDataset[] => {
    if (isEmpty(datasets)) {
        return [];
    }

    // Map datasources to dataset ids
    const datasourcesToDatasetIdsMap = mapDatasourcesToDatasetIds(
        datasets,
        datasources
    );

    const uiDatasets = [];
    for (const dataset of datasets) {
        uiDatasets.push(
            getUiDatasetInternal(dataset, datasourcesToDatasetIdsMap)
        );
    }

    return uiDatasets;
};

export const getUiDatasetDatasource = (
    datasource: Datasource
): UiDatasetDatasource => {
    const uiDatasetDatasource = createEmptyUiDatasetDatasource();

    if (!datasource) {
        return uiDatasetDatasource;
    }

    // Basic properties
    uiDatasetDatasource.id = datasource.id;
    uiDatasetDatasource.name =
        datasource.name || i18n.t("label.no-data-marker");

    return uiDatasetDatasource;
};

export const getUiDatasetDatasources = (
    datasources: Datasource[]
): UiDatasetDatasource[] => {
    if (isEmpty(datasources)) {
        return [];
    }

    const uiDatasetDatasources = [];
    for (const datasource of datasources) {
        uiDatasetDatasources.push(getUiDatasetDatasource(datasource));
    }

    return uiDatasetDatasources;
};

export const getUiDatasetDatasourceId = (
    uiDatasetDatasource: UiDatasetDatasource
): number => {
    if (!uiDatasetDatasource) {
        return -1;
    }

    return uiDatasetDatasource.id;
};

export const getUiDatasetDatasourceName = (
    uiDatasetDatasource: UiDatasetDatasource
): string => {
    if (!uiDatasetDatasource) {
        return "";
    }

    return uiDatasetDatasource.name;
};

export const filterDatasets = (
    uiDatasets: UiDataset[],
    searchWords: string[]
): UiDataset[] => {
    if (isEmpty(uiDatasets)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return uiDatasets;
    }

    const filteredUiDatasets = [];
    for (const uiDataset of uiDatasets) {
        // Only the UI dataset to be searched and not contained dataset
        const uiDatasetCopy = cloneDeep(uiDataset);
        uiDatasetCopy.dataset = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    uiDatasetCopy,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredUiDatasets.push(uiDataset);

                break;
            }
        }
    }

    return filteredUiDatasets;
};

const getUiDatasetInternal = (
    dataset: Dataset,
    datasourcesToDatasetIdsMap: Map<number, UiDatasetDatasource[]>
): UiDataset => {
    const uiDataset = createEmptyUiDataset();
    const noDataMarker = i18n.t("label.no-data-marker");

    // Maintain a copy of dataset
    uiDataset.dataset = dataset;

    // Basic properties
    uiDataset.id = dataset.id;
    uiDataset.name = dataset.name || noDataMarker;

    // Datasources
    uiDataset.datasources =
        (datasourcesToDatasetIdsMap &&
            datasourcesToDatasetIdsMap.get(dataset.id)) ||
        [];
    uiDataset.datasourceCount = formatNumber(uiDataset.datasources.length);

    return uiDataset;
};

const mapDatasourcesToDatasetIds = (
    datasets: Dataset[],
    datasources: Datasource[]
): Map<number, UiDatasetDatasource[]> => {
    const datasourcesToDatasetIdsMap = new Map();

    const datasourceToDatasourceIdsMap = mapDatasourcesToDatasourceIds(
        datasources
    );
    if (isEmpty(datasourceToDatasourceIdsMap)) {
        return datasourcesToDatasetIdsMap;
    }

    for (const dataset of datasets) {
        if (isEmpty(dataset.datasources)) {
            continue;
        }

        for (const datasource of dataset.datasources) {
            const mappedDatasource = datasourceToDatasourceIdsMap.get(
                datasource.id
            );
            if (!mappedDatasource) {
                continue;
            }

            const uiDatasetDatasources = datasourcesToDatasetIdsMap.get(
                dataset.id
            );
            if (uiDatasetDatasources) {
                // Add to existing list
                uiDatasetDatasources.push(mappedDatasource);
            } else {
                // Create and add to list
                datasourcesToDatasetIdsMap.set(dataset.id, [mappedDatasource]);
            }
        }
    }

    return datasourcesToDatasetIdsMap;
};

const mapDatasourcesToDatasourceIds = (
    datasources: Datasource[]
): Map<number, UiDatasetDatasource> => {
    const datasourcesToDatasourceIdsMap = new Map();

    if (isEmpty(datasources)) {
        return datasourcesToDatasourceIdsMap;
    }

    for (const datasource of datasources) {
        datasourcesToDatasourceIdsMap.set(
            datasource.id,
            getUiDatasetDatasource(datasource)
        );
    }

    return datasourcesToDatasourceIdsMap;
};

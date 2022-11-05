import i18n from "i18next";
import { isEmpty } from "lodash";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createEmptyUiDataset = (): UiDataset => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        datasourceId: -1,
        datasourceName: noDataMarker,
    };
};

export const createEmptyDataset = (): Dataset => {
    return {
        name: "",
        dataSource: {
            name: "",
        } as Datasource,
    } as Dataset;
};

export const createEmptyDatasource = (): Datasource => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
    } as Datasource;
};

export const getUiDataset = (dataset: Dataset): UiDataset => {
    const uiDataset = createEmptyUiDataset();

    if (!dataset) {
        return uiDataset;
    }

    const noDataMarker = i18n.t("label.no-data-marker");

    // Basic properties
    uiDataset.id = dataset.id;
    uiDataset.name = dataset.name || noDataMarker;
    uiDataset.active = dataset.active;
    // Datasource properties
    if (dataset.dataSource) {
        uiDataset.datasourceId = dataset.dataSource.id;
        uiDataset.datasourceName = dataset.dataSource.name || noDataMarker;
    }

    return uiDataset;
};

export const getUiDatasets = (datasets: Dataset[]): UiDataset[] => {
    if (isEmpty(datasets)) {
        return [];
    }

    const uiDatasets = [];
    for (const dataset of datasets) {
        uiDatasets.push(getUiDataset(dataset));
    }

    return uiDatasets;
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
        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    uiDataset,
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

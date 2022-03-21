import i18n from "i18next";
import { cloneDeep, isEmpty, omit } from "lodash";
import {
    Datasource,
    DatasourceMetaList,
    DatasourceProperties,
} from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createDefaultDatasource = (): Datasource => {
    return {
        name: "new-datasource",
        type: "datasource",
        properties: {
            zooKeeperURL: "localhost:2123",
            clusterName: "QuickStartCluster",
            controllerConnectionScheme: "http",
            controllerHost: "localhost",
            controllerPort: 9000,
            cacheLoaderClassName:
                "org.apache.pinot.thirdeye.datasource.pinot.PinotControllerResponseCacheLoader",
        } as DatasourceProperties,
        metaList: [
            {
                classRef:
                    "org.apache.pinot.thirdeye.auto.onboard.AutoOnboardPinotMetadataSource",
                properties: {},
            } as DatasourceMetaList,
        ],
    } as Datasource;
};

export const createEmptyUiDatasource = (): UiDatasource => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        type: noDataMarker,
        datasource: null,
    };
};

export const getUiDatasource = (datasource: Datasource): UiDatasource => {
    if (!datasource) {
        return createEmptyUiDatasource();
    }

    return getUiDatasourceInternal(datasource);
};

export const getUiDatasources = (datasources: Datasource[]): UiDatasource[] => {
    if (isEmpty(datasources)) {
        return [];
    }

    const uiDatasources = [];
    for (const datasource of datasources) {
        uiDatasources.push(getUiDatasourceInternal(datasource));
    }

    return uiDatasources;
};

export const filterDatasources = (
    uiDatasources: UiDatasource[],
    searchWords: string[]
): UiDatasource[] => {
    if (isEmpty(uiDatasources)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return uiDatasources;
    }

    const filteredUiDatasources = [];
    for (const uiDatasource of uiDatasources) {
        // Only the UI datasource to be searched and not contained datasource
        const uiDatasourceCopy = cloneDeep(uiDatasource);
        uiDatasourceCopy.datasource = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    uiDatasourceCopy,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredUiDatasources.push(uiDatasource);

                break;
            }
        }
    }

    return filteredUiDatasources;
};

export const omitNonUpdatableData = (datasource: Datasource): Datasource => {
    const newDatasource = omit(datasource, "id");

    return newDatasource as Datasource;
};

const getUiDatasourceInternal = (datasource: Datasource): UiDatasource => {
    const uiDatasource = createEmptyUiDatasource();
    const noDataMarker = i18n.t("label.no-data-marker");

    // Maintain a copy of datasource
    uiDatasource.datasource = datasource;

    // Basic properties
    uiDatasource.id = datasource.id;
    uiDatasource.name = datasource.name || noDataMarker;
    uiDatasource.type = datasource.type || noDataMarker;

    return uiDatasource;
};

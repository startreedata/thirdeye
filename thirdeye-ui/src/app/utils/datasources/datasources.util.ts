// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import i18n from "i18next";
import { cloneDeep, isEmpty, omit } from "lodash";
import {
    Datasource,
    DatasourceProperties,
} from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createDefaultDatasource = (): Datasource => {
    return {
        name: "mypinot",
        type: "pinot",
        properties: {
            zookeeperUrl:
                "pinot-zookeeper-headless.managed.svc.cluster.local:2181",
            clusterName: "pinot",
            controllerConnectionScheme: "https",
            controllerHost:
                "pinot-pinot-controller-headless.managed.svc.cluster.local",
            controllerPort: 9000,
        } as DatasourceProperties,
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

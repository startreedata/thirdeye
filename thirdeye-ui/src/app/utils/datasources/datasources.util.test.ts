/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { cloneDeep } from "lodash";
import {
    Datasource,
    DatasourceMetaList,
    DatasourceProperties,
} from "../../rest/dto/datasource.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import {
    createDefaultDatasource,
    createEmptyUiDatasource,
    filterDatasources,
    getUiDatasource,
    getUiDatasources,
} from "./datasources.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Datasources Util", () => {
    it("createDefaultDatasource should create appropriate datasource", () => {
        expect(createDefaultDatasource()).toEqual(mockDefaultDatasource);
    });

    it("createEmptyUiDatasource should create appropriate UI datasource", () => {
        expect(createEmptyUiDatasource()).toEqual(mockEmptyUiDatasource);
    });

    it("getUiDatasource should return empty UI datasource for invalid datasource", () => {
        expect(getUiDatasource(null as unknown as Datasource)).toEqual(
            mockEmptyUiDatasource
        );
    });

    it("getUiDatasources should return empty array for invalid datasources", () => {
        expect(getUiDatasources(null as unknown as Datasource[])).toEqual([]);
    });

    it("getUiDatasources should return empty array for empty datasources", () => {
        expect(getUiDatasources([])).toEqual([]);
    });

    it("getUiDatasources should return appropriate UI datasources for datasources", () => {
        const expectedUiDatasource1 = cloneDeep(mockUiDatasource1);
        const expectedUiDatasource2 = cloneDeep(mockUiDatasource2);
        const expectedUiDatasource3 = cloneDeep(mockUiDatasource3);

        expect(getUiDatasources(mockDatasources)).toEqual([
            expectedUiDatasource1,
            expectedUiDatasource2,
            expectedUiDatasource3,
        ]);
    });

    it("filterDatasources should return empty array for invalid UI datasources", () => {
        expect(
            filterDatasources(
                null as unknown as UiDatasource[],
                mockSearchWords
            )
        ).toEqual([]);
    });

    it("filterDatasources should return empty array for empty UI datasources", () => {
        expect(filterDatasources([], mockSearchWords)).toEqual([]);
    });

    it("filterDatasources should return appropriate UI datasources for UI datasources and invalid search words", () => {
        expect(
            filterDatasources(mockUiDatasources, null as unknown as string[])
        ).toEqual(mockUiDatasources);
    });

    it("filterDatasources should return appropriate UI datasources for UI datasources and empty search words", () => {
        expect(filterDatasources(mockUiDatasources, [])).toEqual(
            mockUiDatasources
        );
    });

    it("filterDatasources should return appropriate UI datasources for UI datasources and search words", () => {
        expect(filterDatasources(mockUiDatasources, mockSearchWords)).toEqual([
            mockUiDatasource1,
            mockUiDatasource3,
        ]);
    });
});

const mockDefaultDatasource = {
    name: "mypinot",
    type: "pinot",
    properties: {
        zookeeperUrl: "pinot-zookeeper-headless.managed.svc.cluster.local:2181",
        clusterName: "pinot",
        controllerConnectionScheme: "https",
        controllerHost:
            "pinot-pinot-controller-headless.managed.svc.cluster.local",
        controllerPort: 9000,
    },
};

const mockEmptyUiDatasource = {
    id: -1,
    name: "label.no-data-marker",
    type: "label.no-data-marker",
    datasource: null,
};

const mockDatasource1 = {
    id: 1,
    name: "testNameDatasource1",
    type: "testTypeDatasource1",
    properties: {
        zookeeperUrl: "testURLDatasource1",
        clusterName: "testClusterDatasource1",
        controllerConnectionScheme: "http",
        controllerHost: "localhost",
        controllerPort: 9000,
        cacheLoaderClassName: "testCacheLoaderClassNameDatasource1",
    } as DatasourceProperties,
    metaList: [
        {
            classRef: "testClassRefDatasource1",
            properties: {},
        } as DatasourceMetaList,
    ],
} as Datasource;

const mockDatasource2 = {
    id: 8,
    name: "",
    type: "",
    properties: {},
    metaList: [] as DatasourceMetaList[],
} as Datasource;

const mockDatasource3 = {
    id: 10,
    name: "testNameDatasource6",
    type: "testTypeDatasource6",
} as Datasource;

const mockDatasources = [mockDatasource1, mockDatasource2, mockDatasource3];

const mockUiDatasource1 = {
    id: 1,
    name: "testNameDatasource1",
    type: "testTypeDatasource1",
    datasource: mockDatasource1,
};

const mockUiDatasource2 = {
    id: 8,
    name: "label.no-data-marker",
    type: "label.no-data-marker",
    datasource: mockDatasource2,
};

const mockUiDatasource3 = {
    id: 10,
    name: "testNameDatasource6",
    type: "testTypeDatasource6",
    datasource: mockDatasource3,
};

const mockUiDatasources = [
    mockUiDatasource1,
    mockUiDatasource2,
    mockUiDatasource3,
];

const mockSearchWords = ["testNameDatasource1", "testNameDatasource6"];

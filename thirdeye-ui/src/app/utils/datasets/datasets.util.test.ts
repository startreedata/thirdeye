///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import { Dataset } from "../../rest/dto/dataset.interfaces";
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import {
    createEmptyDataset,
    createEmptyUiDataset,
    filterDatasets,
    getUiDataset,
    getUiDatasets,
} from "./datasets.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Datasets Util", () => {
    it("createEmptyUiDataset should create appropriate UI dataset", () => {
        expect(createEmptyUiDataset()).toEqual(mockEmptyUiDataset);
    });

    it("createEmptyDataset should create appropriate dataset", () => {
        expect(createEmptyDataset()).toEqual(mockEmptyDataset);
    });

    it("getUiDataset should return empty UI dataset for invalid dataset", () => {
        expect(getUiDataset(null as unknown as Dataset)).toEqual(
            mockEmptyUiDataset
        );
    });

    it("getUiDataset should return appropriate UI dataset for dataset", () => {
        expect(getUiDataset(mockDataset1)).toEqual(mockUiDataset1);
    });

    it("getUiDatasets should return empty array for invalid dataset", () => {
        expect(getUiDatasets(null as unknown as Dataset[])).toEqual([]);
    });

    it("getUiDatasets should return empty array for empty datasets", () => {
        expect(getUiDatasets([])).toEqual([]);
    });

    it("getUiDatasets should return appropriate UI datasets for datasets", () => {
        expect(getUiDatasets(mockDatasets)).toEqual(mockUiDatasets);
    });

    it("filterDatasets should return empty array for invalid UI datasets", () => {
        expect(
            filterDatasets(null as unknown as UiDataset[], mockSearchWords)
        ).toEqual([]);
    });

    it("filterDatasets should return empty array for empty UI datasets", () => {
        expect(filterDatasets([], mockSearchWords)).toEqual([]);
    });

    it("filterDatasets should return appropriate UI datasets for UI datasets and invalid search words", () => {
        expect(
            filterDatasets(mockUiDatasets, null as unknown as string[])
        ).toEqual(mockUiDatasets);
    });

    it("filterDatasets should return appropriate UI datasets for UI datasets and empty search words", () => {
        expect(filterDatasets(mockUiDatasets, [])).toEqual(mockUiDatasets);
    });

    it("filterDatasets should return appropriate UI datasets for UI datasets and search words", () => {
        expect(filterDatasets(mockUiDatasets, mockSearchWords)).toEqual([
            mockUiDataset1,
        ]);
    });
});

const mockEmptyDataset = {
    name: "",
    dataSource: {
        name: "",
    },
};

const mockEmptyUiDataset = {
    id: -1,
    name: "label.no-data-marker",
    datasourceId: -1,
    datasourceName: "label.no-data-marker",
};

const mockDataset1 = {
    id: 1,
    name: "testNameDataset1",
    dataSource: {
        id: 2,
        name: "testNameDatasource2",
    },
} as Dataset;

const mockDataset2 = {
    id: 5,
    dataSource: {
        id: 3,
    },
} as Dataset;

const mockDataset3 = {
    id: 6,
} as Dataset;

const mockDatasets = [mockDataset1, mockDataset2, mockDataset3];

const mockUiDataset1 = {
    id: 1,
    name: "testNameDataset1",
    datasourceId: 2,
    datasourceName: "testNameDatasource2",
};

const mockUiDataset2 = {
    id: 5,
    name: "label.no-data-marker",
    datasourceId: 3,
    datasourceName: "label.no-data-marker",
};

const mockUiDataset3 = {
    id: 6,
    name: "label.no-data-marker",
    datasourceId: -1,
    datasourceName: "label.no-data-marker",
};

const mockUiDatasets = [mockUiDataset1, mockUiDataset2, mockUiDataset3];

const mockSearchWords = ["testNameDatasource2"];

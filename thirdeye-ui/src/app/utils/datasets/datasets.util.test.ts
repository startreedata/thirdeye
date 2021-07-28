import { cloneDeep } from "lodash";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import {
    UiDataset,
    UiDatasetDatasource,
} from "../../rest/dto/ui-dataset.interfaces";
import {
    createEmptyDataset,
    createEmptyUiDataset,
    createEmptyUiDatasetDatasource,
    filterDatasets,
    getUiDataset,
    getUiDatasetDatasource,
    getUiDatasetDatasourceId,
    getUiDatasetDatasourceName,
    getUiDatasetDatasources,
    getUiDatasets,
} from "./datasets.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

jest.mock("../number/number.util", () => ({
    formatNumber: jest.fn().mockImplementation((num) => num.toString()),
}));

describe("Datasets Util", () => {
    it("createEmptyDataset should create appropriate dataset", () => {
        expect(createEmptyDataset()).toEqual(mockEmptyDataset);
    });

    it("createEmptyUiDataset should create appropriate UI dataset", () => {
        expect(createEmptyUiDataset()).toEqual(mockEmptyUiDataset);
    });

    it("createEmptyUiDatasetDatasource should create appropriate UI dataset datasource", () => {
        expect(createEmptyUiDatasetDatasource()).toEqual(
            mockEmptyUiDatasetDatasource
        );
    });

    it("getUiDataset should return empty UI dataset for invalid dataset", () => {
        expect(
            getUiDataset((null as unknown) as Dataset, mockDatasources)
        ).toEqual(mockEmptyUiDataset);
    });

    it("getUiDataset should return appropriate UI dataset for dataset and invalid datasources", () => {
        const expectedUiDataset = cloneDeep(mockUiDataset1);
        expectedUiDataset.datasources = [];
        expectedUiDataset.datasourceCount = "0";

        expect(
            getUiDataset(mockDataset1, (null as unknown) as Datasource[])
        ).toEqual(expectedUiDataset);
    });

    it("getUiDataset should return appropriate UI dataset for dataset and empty datasources", () => {
        const expectedUiDataset = cloneDeep(mockUiDataset1);
        expectedUiDataset.datasources = [];
        expectedUiDataset.datasourceCount = "0";

        expect(getUiDataset(mockDataset1, [])).toEqual(expectedUiDataset);
    });

    it("getUiDataset should return appropriate UI dataset for dataset and datasources", () => {
        expect(getUiDataset(mockDataset1, mockDatasources)).toEqual(
            mockUiDataset1
        );
    });

    it("getUiDatasets should return empty array for invalid datasets", () => {
        expect(
            getUiDatasets((null as unknown) as Dataset[], mockDatasources)
        ).toEqual([]);
    });

    it("getUiDatasets should return empty array for empty datasets", () => {
        expect(getUiDatasets([], mockDatasources)).toEqual([]);
    });

    it("getUiDatasets should return appropriate UI datasets for datasets and invalid datasources", () => {
        const expectedUiDataset1 = cloneDeep(mockUiDataset1);
        expectedUiDataset1.datasources = [];
        expectedUiDataset1.datasourceCount = "0";
        const expectedUiDataset2 = cloneDeep(mockUiDataset2);
        expectedUiDataset2.datasources = [];
        expectedUiDataset2.datasourceCount = "0";
        const expectedUiDataset3 = cloneDeep(mockUiDataset3);
        expectedUiDataset3.datasources = [];
        expectedUiDataset3.datasourceCount = "0";

        expect(
            getUiDatasets(mockDatasets, (null as unknown) as Datasource[])
        ).toEqual([expectedUiDataset1, expectedUiDataset2, expectedUiDataset3]);
    });

    it("getUiDatasets should return appropriate UI datasets for datasets and empty datasources", () => {
        const expectedUiDataset1 = cloneDeep(mockUiDataset1);
        expectedUiDataset1.datasources = [];
        expectedUiDataset1.datasourceCount = "0";
        const expectedUiDataset2 = cloneDeep(mockUiDataset2);
        expectedUiDataset2.datasources = [];
        expectedUiDataset2.datasourceCount = "0";
        const expectedUiDataset3 = cloneDeep(mockUiDataset3);
        expectedUiDataset3.datasources = [];
        expectedUiDataset3.datasourceCount = "0";

        expect(getUiDatasets(mockDatasets, [])).toEqual([
            expectedUiDataset1,
            expectedUiDataset2,
            expectedUiDataset3,
        ]);
    });

    it("getUiDatasets should return appropriate UI datasets for datasets and datasources", () => {
        expect(getUiDatasets(mockDatasets, mockDatasources)).toEqual(
            mockUiDatasets
        );
    });

    it("getUiDatasetDatasource should return empty UI dataset datasource for invalid datasource", () => {
        expect(getUiDatasetDatasource((null as unknown) as Datasource)).toEqual(
            mockEmptyUiDatasetDatasource
        );
    });

    it("getUiDatasetDatasource should return appropriate UI dataset datasource for datasource", () => {
        expect(getUiDatasetDatasource(mockDatasource1)).toEqual(
            mockUiDatasetDatasource1
        );
    });

    it("getUiDatasetDatasources should return empty array for invalid datasources", () => {
        expect(
            getUiDatasetDatasources((null as unknown) as Datasource[])
        ).toEqual([]);
    });

    it("getUiDatasetDatasources should return empty array for empty datasources", () => {
        expect(getUiDatasetDatasources([])).toEqual([]);
    });

    it("getUiDatasetDatasources should return appropriate UI dataset datasources for datasources", () => {
        expect(getUiDatasetDatasources(mockDatasources)).toEqual(
            mockUiDatasetDatasources
        );
    });

    it("getUiDatasetDatasourceId should return -1 for invalid UI dataset datasource", () => {
        expect(
            getUiDatasetDatasourceId((null as unknown) as UiDatasetDatasource)
        ).toEqual(-1);
    });

    it("getUiDatasetDatasourceId should return approopriate id for UI dataset datasource", () => {
        expect(getUiDatasetDatasourceId(mockUiDatasetDatasource1)).toEqual(2);
    });

    it("getUiDatasetDatasourceName should return empty string for invalid UI dataset datasource", () => {
        expect(
            getUiDatasetDatasourceName((null as unknown) as UiDatasetDatasource)
        ).toEqual("");
    });

    it("getUiDatasetDatasourceName should return approopriate name for UI dataset datasource", () => {
        expect(getUiDatasetDatasourceName(mockUiDatasetDatasource1)).toEqual(
            "testNameDatasource2"
        );
    });

    it("filterDatasets should return empty array for invalid UI datasets", () => {
        expect(
            filterDatasets((null as unknown) as UiDataset[], mockSearchWords)
        ).toEqual([]);
    });

    it("filterDatasets should return empty array for empty UI datasets", () => {
        expect(filterDatasets([], mockSearchWords)).toEqual([]);
    });

    it("filterDatasets should return appropriate UI datasets for UI datasets and invalid search words", () => {
        expect(
            filterDatasets(mockUiDatasets, (null as unknown) as string[])
        ).toEqual(mockUiDatasets);
    });

    it("filterDatasets should return appropriate UI datasets for UI datasets and empty search words", () => {
        expect(filterDatasets(mockUiDatasets, [])).toEqual(mockUiDatasets);
    });

    it("filterDatasets should return appropriate UI datasets for UI datasets and search words", () => {
        expect(filterDatasets(mockUiDatasets, mockSearchWords)).toEqual([
            mockUiDataset1,
            mockUiDataset3,
        ]);
    });
});

const mockEmptyDataset = {
    name: "",
    datasources: [],
};

const mockEmptyUiDataset = {
    id: -1,
    name: "label.no-data-marker",
    datasources: [],
    datasourceCount: "0",
    dataset: null,
};

const mockEmptyUiDatasetDatasource = {
    id: -1,
    name: "label.no-data-marker",
};

const mockDataset1 = {
    id: 1,
    name: "testNameDataset1",
    datasources: [
        {
            id: 2,
        },
        {
            id: 3,
        },
        {
            id: 4,
        },
    ],
} as Dataset;

const mockDataset2 = {
    id: 5,
    datasources: [] as Datasource[],
} as Dataset;

const mockDataset3 = {
    id: 6,
    name: "testNameDataset6",
} as Dataset;

const mockDatasets = [mockDataset1, mockDataset2, mockDataset3];

const mockDatasource1 = {
    id: 2,
    name: "testNameDatasource2",
} as Datasource;

const mockDatasource2 = {
    id: 3,
} as Datasource;

const mockDatasource3 = {
    id: 6,
    name: "testNameDatasource6",
} as Datasource;

const mockDatasources = [mockDatasource1, mockDatasource2, mockDatasource3];

const mockUiDatasetDatasource1 = {
    id: 2,
    name: "testNameDatasource2",
};

const mockUiDatasetDatasource2 = {
    id: 3,
    name: "label.no-data-marker",
};

const mockUiDatasetDatasource3 = {
    id: 6,
    name: "testNameDatasource6",
};

const mockUiDatasetDatasources = [
    mockUiDatasetDatasource1,
    mockUiDatasetDatasource2,
    mockUiDatasetDatasource3,
];

const mockUiDataset1 = {
    id: 1,
    name: "testNameDataset1",
    datasources: [
        {
            id: 2,
            name: "testNameDatasource2",
        },
        {
            id: 3,
            name: "label.no-data-marker",
        },
    ],
    datasourceCount: "2",
    dataset: mockDataset1,
};

const mockUiDataset2 = {
    id: 5,
    name: "label.no-data-marker",
    datasources: [],
    datasourceCount: "0",
    dataset: mockDataset2,
};

const mockUiDataset3 = {
    id: 6,
    name: "testNameDataset6",
    datasources: [],
    datasourceCount: "0",
    dataset: mockDataset3,
};

const mockUiDatasets = [mockUiDataset1, mockUiDataset2, mockUiDataset3];

const mockSearchWords = ["testNameDatasource2", "testNameDataset6"];

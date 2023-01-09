/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    act,
    cleanup,
    fireEvent,
    render,
    screen,
} from "@testing-library/react";
import React from "react";
import {
    DataGridColumnV1,
    DataGridSortOrderV1,
} from "../../platform/components/data-grid-v1/data-grid-v1";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { AnomalyListV1 } from "./anomaly-list-v1.component";
import { AnomalyListV1Props } from "./anomaly-list-v1.interfaces";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../platform/components/page-v1", () => ({
    PageContentsCardV1: jest.fn().mockImplementation((props) => props.children),
}));

jest.mock("../../platform/utils", () => ({
    linkRendererV1: jest
        .fn()
        .mockImplementation((value: string, id: number) => (
            <a href={`testHref${id}`}>{value}</a>
        )),
}));

jest.mock("../anomaly-quick-filters/anomaly-quick-filters.component", () => ({
    AnomalyQuickFilters: jest.fn().mockImplementation(() => <>testFilters</>),
}));

jest.mock("../../platform/components/data-grid-v1", () => ({
    DataGridV1: jest.fn().mockImplementation((props) => (
        <>
            {Array.isArray(props.data) && props.data.length ? (
                props.data.map((anomaly: UiAnomaly) => {
                    const mockAnomaly = { ...anomaly };

                    return (
                        <span key={mockAnomaly.id}>
                            {props.toolbarComponent}
                            <p
                                onClick={() =>
                                    props.onSelectionChange({
                                        rowKeyValues: [1],
                                        rowKeyValueMap: new Map().set(
                                            1,
                                            anomaly
                                        ),
                                    })
                                }
                            >
                                testSelection{mockAnomaly.id}
                            </p>
                            {Array.isArray(props.columns) &&
                            props.columns.length
                                ? props.columns.map(
                                      (column: DataGridColumnV1<UiAnomaly>) => (
                                          <React.Fragment key={column.key}>
                                              {column.customCellRenderer &&
                                                  column.customCellRenderer(
                                                      anomaly[
                                                          column.key as keyof UiAnomaly
                                                      ] as unknown as Record<
                                                          string,
                                                          unknown
                                                      >,
                                                      anomaly,
                                                      column
                                                  )}
                                          </React.Fragment>
                                      )
                                  )
                                : null}
                        </span>
                    );
                })
            ) : (
                <p>NoDataIndicator</p>
            )}
        </>
    )),
    DataGridScrollV1: {
        Body: jest.fn().mockImplementation((props) => props.children),
    },
    DataGridSortOrderV1: DataGridSortOrderV1,
}));

jest.mock("../../utils/routes/routes.util", () => ({
    getAnomaliesAnomalyPath: jest.fn().mockImplementation((value) => value),
    getAlertsViewPath: jest.fn().mockImplementation((value) => value),
    getAlertsAlertPath: jest.fn().mockImplementation((value) => value),
}));

describe("AnomalyListV1", () => {
    let mockProps = { ...mockDefaultProps };

    beforeEach(() => cleanup);

    afterEach(() => {
        mockProps = { ...mockDefaultProps };
    });

    it("component should load with no anomalies", async () => {
        const props = { ...mockProps, anomalies: [] };
        act(() => {
            render(<AnomalyListV1 {...props} />);
        });

        expect(await screen.findByText("NoDataIndicator")).toBeInTheDocument();
    });

    it("component should load with anomalies", async () => {
        const props = { ...mockProps };
        act(() => {
            render(<AnomalyListV1 {...props} />);
        });

        expect(await screen.findByText("testAnomaly")).toBeInTheDocument();
    });

    it("delete button should be disabled if selection is none", async () => {
        const props = { ...mockProps };
        act(() => {
            render(<AnomalyListV1 {...props} />);
        });

        expect(screen.getByTestId("button-delete")).toHaveAttribute("disabled");

        expect(mockMethod).not.toHaveBeenCalled();
    });

    it("component should call onDelete when Delete is clicked", async () => {
        const props = { ...mockProps };
        act(() => {
            render(<AnomalyListV1 {...props} />);
        });

        fireEvent.click(screen.getByText("testSelection1"));

        expect(screen.getByTestId("button-delete")).not.toHaveAttribute(
            "disabled"
        );

        fireEvent.click(screen.getByTestId("button-delete"));

        expect(mockMethod).toHaveBeenNthCalledWith(1, [mockUiAnomaly]);
    });

    it("component should render link with appropriate href", async () => {
        const props = { ...mockProps };
        act(() => {
            render(<AnomalyListV1 {...props} />);
        });

        expect(screen.getByText("testAnomaly")).toBeInTheDocument();
        expect(screen.getByText("testAnomaly")).toHaveAttribute(
            "href",
            "testHref1"
        );
    });
});

const mockUiAnomaly = {
    id: 1,
    name: "testAnomaly",
    alertName: "testAlert",
    alertId: 2,
} as UiAnomaly;

const mockMethod = jest.fn();

const mockDefaultProps = {
    anomalies: [mockUiAnomaly],
    onDelete: mockMethod,
} as AnomalyListV1Props;

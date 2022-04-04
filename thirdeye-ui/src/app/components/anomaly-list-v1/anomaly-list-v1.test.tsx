import {
    act,
    cleanup,
    fireEvent,
    render,
    screen,
} from "@testing-library/react";
import React from "react";
import { DataGridColumnV1 } from "../../platform/components/data-grid-v1/data-grid-v1";
import { Alert } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { Metric } from "../../rest/dto/metric.interfaces";
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
    formatDurationV1: jest
        .fn()
        .mockImplementation(
            (startTime, endTime) =>
                `${startTime.toString()} ${endTime.toString()}`
        ),
    formatDateAndTimeV1: jest
        .fn()
        .mockImplementation((date) => date.toString()),
    formatLargeNumberV1: jest.fn().mockImplementation((num) => num.toString()),
    formatPercentageV1: jest.fn().mockImplementation((num) => num.toString()),
}));

jest.mock("../../platform/components/data-grid-v1", () => ({
    DataGridV1: jest.fn().mockImplementation((props) => (
        <>
            {Array.isArray(props.data) && props.data.length ? (
                props.data.map((anomaly: Anomaly) => {
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
                                      (column: DataGridColumnV1<Anomaly>) =>
                                          column.customCellRenderer &&
                                          column.customCellRenderer(
                                              anomaly[
                                                  column.key as keyof Anomaly
                                              ] as unknown as Record<
                                                  string,
                                                  unknown
                                              >,
                                              anomaly,
                                              column
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
}));

jest.mock("../../utils/routes/routes.util", () => ({
    getAnomaliesAnomalyPath: jest.fn().mockImplementation((value) => value),
    getAlertsViewPath: jest.fn().mockImplementation((value) => value),
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

        expect(mockMethod).toHaveBeenNthCalledWith(1, mockAnomaly);
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

const mockAnomaly = {
    id: 1,
    startTime: 1,
    endTime: 2,
    avgCurrentVal: 1,
    avgBaselineVal: 1,
    score: 1,
    weight: 1,
    impactToGlobal: 1,
    sourceType: "DEFAULT_ANOMALY_DETECTION",
    created: 1,
    notified: true,
    message: "message",
    alert: {
        description: "desc",
        cron: "cron",
        id: 1,
        name: "testAnomaly",
    } as Alert,
    metric: {} as Metric,
    children: [],
    type: "DEVIATION",
    severity: "CRITICAL",
    child: false,
} as Anomaly;

const mockMethod = jest.fn();

const mockDefaultProps = {
    anomalies: [mockAnomaly],
    onDelete: mockMethod,
} as AnomalyListV1Props;

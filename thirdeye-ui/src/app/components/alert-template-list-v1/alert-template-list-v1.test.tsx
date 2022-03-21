import { fireEvent, render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import { AlertTemplateListV1 } from "./alert-template-list-v1.component";
import { AlertTemplateListV1Props } from "./alert-template-list-v1.interfaces";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("react-router-dom", () => ({
    useNavigate: jest.fn().mockReturnValue(() => null),
}));

jest.mock("../entity-cards/alert-card-v1/alert-card-v1.component", () => ({
    AlertCardV1: jest.fn().mockImplementation((props) => props.uiAlert),
}));

jest.mock("../../platform/components", () => ({
    JSONEditorV1: jest.fn().mockImplementation((props) => props.children),
    PageContentsCardV1: jest.fn().mockImplementation((props) => props.children),
    DataGridV1: jest.fn().mockImplementation((props) => (
        <>
            {Array.isArray(props.data) && props.data.length ? (
                props.data.map(
                    (
                        alertTemplate: AlertTemplate & {
                            children?: {
                                id: number;
                                expandPanelContents: ReactNode;
                            };
                        }
                    ) => {
                        const mockAlertTemplate = { ...alertTemplate };
                        delete mockAlertTemplate.children;

                        return (
                            <span key={alertTemplate.id}>
                                {alertTemplate.name}
                                <p
                                    onClick={() =>
                                        mockOnChangeMethod(mockAlertTemplate)
                                    }
                                >{`Edit${alertTemplate.id}`}</p>
                                <p
                                    onClick={() =>
                                        mockOnDeleteMethod(mockAlertTemplate)
                                    }
                                >{`Delete${alertTemplate.id}`}</p>
                            </span>
                        );
                    }
                )
            ) : (
                <p>NoDataIndicator</p>
            )}
        </>
    )),
    DataGridScrollV1: {
        Body: jest.fn().mockImplementation((props) => props.children),
    },
}));

describe("AlertTemplateListV1", () => {
    it("should load with no alert templates", async () => {
        const props = { ...mockDefaultProps, alertTemplates: [] };
        render(<AlertTemplateListV1 {...props} />);

        expect(await screen.findByText("NoDataIndicator")).toBeInTheDocument();
    });

    it("should load with alert templates", async () => {
        render(<AlertTemplateListV1 {...mockDefaultProps} />);

        expect(
            await screen.findByText("threshold-template")
        ).toBeInTheDocument();
    });

    it("should call onChange when Edit is clicked", async () => {
        render(<AlertTemplateListV1 {...mockDefaultProps} />);

        fireEvent.click(screen.getByText("Edit1"));

        expect(mockOnChangeMethod).toHaveBeenNthCalledWith(
            1,
            mockAlertTemplate
        );
    });

    it("should call onDelete when Delete is clicked", async () => {
        render(<AlertTemplateListV1 {...mockDefaultProps} />);

        fireEvent.click(screen.getByText("Delete1"));

        expect(mockOnDeleteMethod).toHaveBeenNthCalledWith(
            1,
            mockAlertTemplate
        );
    });
});

const mockAlertTemplate = {
    id: 1,
    name: "threshold-template",
    description: "Threshold Template",
    cron: "0 0/1 * 1/1 * ? * ",
    nodes: [
        {
            name: "root",
            type: "AnomalyDetector",
            params: {
                "component.min": "${min}",
                "component.monitoringGranularity": "PT2H",
                "component.metric": "met",
                "component.max": "${max}",
                "component.timestamp": "ts",
                "anomaly.metric": "${metric}",
                "component.timezone": "US/Pacific",
                type: "THRESHOLD",
                "component.dimensions": [],
                "component.offset": "mo1m",
                "component.pattern": "down",
            },
            inputs: [
                {
                    targetProperty: "current",
                    sourcePlanNode: "currentDataFetcher",
                    sourceProperty: "currentOutput",
                },
            ],
            outputs: [],
        },
        {
            name: "currentDataFetcher",
            type: "DataFetcher",
            params: {
                "component.dataSource": "${dataSource}",
                "component.query": "SELECT !",
            },
            inputs: [],
            outputs: [
                {
                    outputKey: "pinot",
                    outputName: "currentOutput",
                },
            ],
        },
    ],
    rca: {
        datasource: "${dataSource}",
        dataset: "${dataset}",
        metric: "${metricColumn}",
    },
};

const mockOnDeleteMethod = jest.fn();

const mockOnChangeMethod = jest.fn();

const mockDefaultProps = {
    alertTemplates: [mockAlertTemplate],
    onChange: mockOnChangeMethod,
    onDelete: mockOnDeleteMethod,
} as AlertTemplateListV1Props;

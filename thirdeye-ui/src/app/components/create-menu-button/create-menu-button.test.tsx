import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { CreateMenuButton } from "./create-menu-button.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string, additionalDetails?: { entity: string }) =>
            additionalDetails?.entity || key,
    }),
}));

jest.mock("../../utils/routes/routes.util", () => ({
    getAlertsCreatePath: jest.fn().mockReturnValue("create-alert"),
    getDatasetsOnboardPath: jest.fn().mockReturnValue("onboard-dataset"),
    getDatasourcesCreatePath: jest.fn().mockReturnValue("create-datasource"),
    getMetricsCreatePath: jest.fn().mockReturnValue("create-metric"),
    getSubscriptionGroupsCreatePath: jest
        .fn()
        .mockReturnValue("create-subscription-group"),
}));

jest.mock("react-router-dom", () => ({
    useLocation: jest.fn().mockReturnValue({ pathname: "test" }),
}));

jest.mock("react-router", () => ({
    useHistory: jest.fn().mockImplementation(() => ({ push: mockPush })),
}));

describe("CreateMenuButton", () => {
    beforeEach(() => cleanup);

    it("should render CreateMenuButton", () => {
        render(<CreateMenuButton />);

        expect(screen.getByText("label.create")).toBeInTheDocument();
    });

    it("should render dropdown options when click on create button", () => {
        render(<CreateMenuButton />);

        expect(screen.getByText("label.create")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.create"));

        expect(screen.getByText("label.alert")).toBeInTheDocument();
        expect(
            screen.getByText("label.subscription-group")
        ).toBeInTheDocument();
        expect(screen.getByText("label.metric")).toBeInTheDocument();
        expect(screen.getByText("label.dataset")).toBeInTheDocument();
        expect(screen.getByText("label.datasource")).toBeInTheDocument();
    });

    it("should call history push with create-alert", () => {
        render(<CreateMenuButton />);

        expect(screen.getByText("label.create")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.create"));

        expect(screen.getByText("label.alert")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.alert"));

        expect(mockPush).toHaveBeenCalledWith("create-alert");
    });

    it("should call history push with create-subscription-group", () => {
        render(<CreateMenuButton />);

        expect(screen.getByText("label.create")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.create"));

        expect(
            screen.getByText("label.subscription-group")
        ).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.subscription-group"));

        expect(mockPush).toHaveBeenCalledWith("create-subscription-group");
    });

    it("should call history push with create-metric", () => {
        render(<CreateMenuButton />);

        expect(screen.getByText("label.create")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.create"));

        expect(screen.getByText("label.metric")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.metric"));

        expect(mockPush).toHaveBeenCalledWith("create-metric");
    });

    it("should call history push with create-dataset", () => {
        render(<CreateMenuButton />);

        expect(screen.getByText("label.create")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.create"));

        expect(screen.getByText("label.dataset")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.dataset"));

        expect(mockPush).toHaveBeenCalledWith("onboard-dataset");
    });

    it("should call history push with create-datasource", () => {
        render(<CreateMenuButton />);

        expect(screen.getByText("label.create")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.create"));

        expect(screen.getByText("label.datasource")).toBeInTheDocument();

        fireEvent.click(screen.getByText("label.datasource"));

        expect(mockPush).toHaveBeenCalledWith("create-datasource");
    });
});

const mockPush = jest.fn();

import { act, render, screen } from "@testing-library/react";
import React from "react";
import { AuthExceptionCodeV1 } from "../../platform/components/auth-provider-v1/auth-provider-v1.interfaces";
import { AuthExceptionCodeV1Label } from "../../platform/utils";
import { LoginPage } from "./login-page.component";

jest.mock("../../platform/components/app-loading-indicator-v1", () => ({
    AppLoadingIndicatorV1: jest
        .fn()
        .mockReturnValue("testAppLoadingIndicatorV1"),
}));

jest.mock("../../platform/components/auth-provider-v1", () => ({
    useAuthProviderV1: jest.fn().mockImplementation(() => ({
        authExceptionCode: mockAuthExceptionCode,
        login: mockLogin,
    })),
}));

jest.mock("../../platform/components/notification-provider-v1", () => ({
    useNotificationProviderV1: jest
        .fn()
        .mockImplementation(() => ({ notify: mockNotify })),
    NotificationTypeV1: {
        Error: "error",
    },
}));

jest.mock("../../platform/components/page-v1", () => ({
    PageV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderTextV1: jest.fn().mockImplementation((props) => props.children),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockImplementation(() => ({
        t: mockT,
    })),
}));

describe("Login Page", () => {
    it("should invoke login when no blocking auth exception", () => {
        mockAuthExceptionCode = AuthExceptionCodeV1.UnauthorizedAccess;
        act(() => {
            render(<LoginPage />);
        });

        expect(mockLogin).toHaveBeenCalled();
    });

    it("should not invoke login when blocking auth exception", () => {
        mockAuthExceptionCode = AuthExceptionCodeV1.InfoCallFailure;
        act(() => {
            render(<LoginPage />);
        });

        expect(mockLogin).not.toHaveBeenCalled();
    });

    it("should notify blocking auth exception", () => {
        mockAuthExceptionCode = AuthExceptionCodeV1.InfoCallFailure;
        act(() => {
            render(<LoginPage />);
        });

        expect(mockNotify).toHaveBeenCalledWith(
            "error",
            "message.authentication-error",
            true
        );
        expect(mockT).toHaveBeenCalledWith("message.authentication-error", {
            exceptionCode:
                AuthExceptionCodeV1Label[AuthExceptionCodeV1.InfoCallFailure],
        });
    });

    it("should not notify non-blocking auth exception", () => {
        mockAuthExceptionCode = AuthExceptionCodeV1.UnauthorizedAccess;
        act(() => {
            render(<LoginPage />);
        });

        expect(mockNotify).not.toHaveBeenCalledWith();
    });

    it("should render loading indicator when no blocking auth exception", () => {
        mockAuthExceptionCode = AuthExceptionCodeV1.UnauthorizedAccess;
        act(() => {
            render(<LoginPage />);
        });

        expect(
            screen.getByText("testAppLoadingIndicatorV1")
        ).toBeInTheDocument();
    });

    it("should render appropriately when blocking auth exception", () => {
        mockAuthExceptionCode = AuthExceptionCodeV1.InfoCallFailure;
        act(() => {
            render(<LoginPage />);
        });

        expect(
            screen.getByText("label.authentication-error")
        ).toBeInTheDocument();
    });
});

let mockAuthExceptionCode = "";

const mockLogin = jest.fn();

const mockNotify = jest.fn().mockReturnValue("ok");

const mockT = jest.fn().mockImplementation((key) => key);

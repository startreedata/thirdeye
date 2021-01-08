import { act, renderHook } from "@testing-library/react-hooks";
import { Breadcrumb } from "../../components/app-breadcrumbs/app-breadcrumbs.interfaces";
import { useAppBreadcrumbsStore } from "./app-breadcrumbs-store";

describe("App Breadcrumbs Store", () => {
    test("should initialize default values", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());

        expect(result.current.appBreadcrumbs).toEqual([]);
    });

    test("setAppSectionBreadcrumbs should update store appropriately with empty page breadcrumbs", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());
        act(() => {
            result.current.setAppSectionBreadcrumbs([
                mockAppSectionBreadcrumb1,
                mockAppSectionBreadcrumb2,
            ]);
            result.current.setPageBreadcrumbs([]);
        });

        expect(result.current.appBreadcrumbs).toEqual([
            mockAppSectionBreadcrumb1,
            mockAppSectionBreadcrumb2,
        ]);
    });

    test("setAppSectionBreadcrumbs should update store appropriately with page breadcrumbs", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());
        act(() => {
            result.current.setAppSectionBreadcrumbs([
                mockAppSectionBreadcrumb1,
                mockAppSectionBreadcrumb2,
            ]);
            result.current.setPageBreadcrumbs([
                mockPageBreadcrumb1,
                mockPageBreadcrumb2,
            ]);
        });

        expect(result.current.appBreadcrumbs).toEqual([
            mockAppSectionBreadcrumb1,
            mockAppSectionBreadcrumb2,
            mockPageBreadcrumb1,
            mockPageBreadcrumb2,
        ]);
    });

    test("setPageBreadcrumbs should update store appropriately with empty app section breadcrumbs", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());
        act(() => {
            result.current.setAppSectionBreadcrumbs([]);
            result.current.setPageBreadcrumbs([
                mockPageBreadcrumb1,
                mockPageBreadcrumb2,
            ]);
        });

        expect(result.current.appBreadcrumbs).toEqual([
            mockPageBreadcrumb1,
            mockPageBreadcrumb2,
        ]);
    });
});

test("setPageBreadcrumbs should update store appropriately with app section breadcrumbs", () => {
    const { result } = renderHook(() => useAppBreadcrumbsStore());
    act(() => {
        result.current.setAppSectionBreadcrumbs([
            mockAppSectionBreadcrumb1,
            mockAppSectionBreadcrumb2,
        ]);
        result.current.setPageBreadcrumbs([
            mockPageBreadcrumb1,
            mockPageBreadcrumb2,
        ]);
    });

    expect(result.current.appBreadcrumbs).toEqual([
        mockAppSectionBreadcrumb1,
        mockAppSectionBreadcrumb2,
        mockPageBreadcrumb1,
        mockPageBreadcrumb2,
    ]);
});

test("pushPageBreadcrumb should update store appropriately", () => {
    const { result } = renderHook(() => useAppBreadcrumbsStore());
    act(() => {
        result.current.setAppSectionBreadcrumbs([mockAppSectionBreadcrumb1]);
        result.current.setPageBreadcrumbs([mockPageBreadcrumb1]);
        result.current.pushPageBreadcrumb(mockPageBreadcrumb2);
    });

    expect(result.current.appBreadcrumbs).toEqual([
        mockAppSectionBreadcrumb1,
        mockPageBreadcrumb1,
        mockPageBreadcrumb2,
    ]);
});

test("popPageBreadcrumb should update store appropriately", () => {
    const { result } = renderHook(() => useAppBreadcrumbsStore());
    act(() => {
        result.current.setAppSectionBreadcrumbs([mockAppSectionBreadcrumb1]);
        result.current.setPageBreadcrumbs([
            mockPageBreadcrumb1,
            mockPageBreadcrumb2,
        ]);
        result.current.popPageBreadcrumb();
    });

    expect(result.current.appBreadcrumbs).toEqual([
        mockAppSectionBreadcrumb1,
        mockPageBreadcrumb1,
    ]);
});

const mockAppSectionBreadcrumb1: Breadcrumb = {
    text: "testAppSectionBreadcrumbText1",
};

const mockAppSectionBreadcrumb2: Breadcrumb = {
    text: "testAppSectionBreadcrumbText2",
};

const mockPageBreadcrumb1: Breadcrumb = {
    text: "testPageBreadcrumbText1",
};

const mockPageBreadcrumb2: Breadcrumb = {
    text: "testPageBreadcrumbText2",
};

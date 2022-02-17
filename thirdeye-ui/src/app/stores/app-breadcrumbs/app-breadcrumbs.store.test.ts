import { act, renderHook } from "@testing-library/react-hooks";
import { Breadcrumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";
import { useAppBreadcrumbsStore } from "./app-breadcrumbs.store";

describe("App Breadcrumbs Store", () => {
    it("should initialize default values", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());

        expect(result.current.routerBreadcrumbs).toEqual([]);
        expect(result.current.pageBreadcrumbs).toEqual([]);
    });

    it("setRouterBreadcrumbs should not update store for invalid breadcrumbs", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());

        expect(result.current.routerBreadcrumbs).toEqual([]);
        expect(result.current.pageBreadcrumbs).toEqual([]);

        act(() => {
            result.current.setRouterBreadcrumbs(
                null as unknown as Breadcrumb[]
            );
        });

        expect(result.current.routerBreadcrumbs).toEqual([]);
        expect(result.current.pageBreadcrumbs).toEqual([]);
    });

    it("setPageBreadcrumbs should not update store for invalid breadcrumbs", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());

        expect(result.current.routerBreadcrumbs).toEqual([]);
        expect(result.current.pageBreadcrumbs).toEqual([]);

        act(() => {
            result.current.setPageBreadcrumbs(null as unknown as Breadcrumb[]);
        });

        expect(result.current.routerBreadcrumbs).toEqual([]);
        expect(result.current.pageBreadcrumbs).toEqual([]);
    });

    it("pushPageBreadcrumb should not update store for invalid breadcrumb", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());

        expect(result.current.routerBreadcrumbs).toEqual([]);
        expect(result.current.pageBreadcrumbs).toEqual([]);

        act(() => {
            result.current.pushPageBreadcrumb(null as unknown as Breadcrumb);
        });

        expect(result.current.routerBreadcrumbs).toEqual([]);
        expect(result.current.pageBreadcrumbs).toEqual([]);
    });

    it("setRouterBreadcrumbs should update store appropriately for breadcrumbs", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());
        act(() => {
            result.current.setRouterBreadcrumbs([
                mockRouterBreadcrumb1,
                mockRouterBreadcrumb2,
            ]);
            result.current.setPageBreadcrumbs([]);
        });

        expect(result.current.routerBreadcrumbs).toEqual([
            mockRouterBreadcrumb1,
            mockRouterBreadcrumb2,
        ]);
        expect(result.current.pageBreadcrumbs).toEqual([]);
    });

    it("setPageBreadcrumbs should update store appropriately for braedcrumbs", () => {
        const { result } = renderHook(() => useAppBreadcrumbsStore());
        act(() => {
            result.current.setRouterBreadcrumbs([]);
            result.current.setPageBreadcrumbs([
                mockPageBreadcrumb1,
                mockPageBreadcrumb2,
            ]);
        });

        expect(result.current.routerBreadcrumbs).toEqual([]);
        expect(result.current.pageBreadcrumbs).toEqual([
            mockPageBreadcrumb1,
            mockPageBreadcrumb2,
        ]);
    });
});

it("pushPageBreadcrumb should update store appropriately for breadcrumb", () => {
    const { result } = renderHook(() => useAppBreadcrumbsStore());
    act(() => {
        result.current.setRouterBreadcrumbs([]);
        result.current.setPageBreadcrumbs([mockPageBreadcrumb1]);
        result.current.pushPageBreadcrumb(mockPageBreadcrumb2);
    });

    expect(result.current.routerBreadcrumbs).toEqual([]);
    expect(result.current.pageBreadcrumbs).toEqual([
        mockPageBreadcrumb1,
        mockPageBreadcrumb2,
    ]);
});

it("popPageBreadcrumb should update empty store appropriately", () => {
    const { result } = renderHook(() => useAppBreadcrumbsStore());
    act(() => {
        result.current.setRouterBreadcrumbs([]);
        result.current.setPageBreadcrumbs([]);
        result.current.popPageBreadcrumb();
    });

    expect(result.current.routerBreadcrumbs).toEqual([]);
    expect(result.current.pageBreadcrumbs).toEqual([]);
});

it("popPageBreadcrumb should update store appropriately", () => {
    const { result } = renderHook(() => useAppBreadcrumbsStore());
    act(() => {
        result.current.setRouterBreadcrumbs([]);
        result.current.setPageBreadcrumbs([
            mockPageBreadcrumb1,
            mockPageBreadcrumb2,
        ]);
        result.current.popPageBreadcrumb();
    });

    expect(result.current.routerBreadcrumbs).toEqual([]);
    expect(result.current.pageBreadcrumbs).toEqual([mockPageBreadcrumb1]);
});

const mockRouterBreadcrumb1 = {
    text: "testTextRouterBreadcrumb1",
};

const mockRouterBreadcrumb2 = {
    text: "testTextRouterBreadcrumb2",
};

const mockPageBreadcrumb1 = {
    text: "testTextPageBreadcrumb1",
};

const mockPageBreadcrumb2 = {
    text: "testTextPageBreadcrumb2",
};

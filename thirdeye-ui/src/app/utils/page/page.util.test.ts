import i18n from "i18next";
import { getDocumentTitle } from "./page.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Page Util", () => {
    it("getDocumentTitle should return appropriate document title for invalid router breadcrumb text, invalid page title and invalid page breadcrumb text", () => {
        expect(
            getDocumentTitle(
                null as unknown as string,
                null as unknown as string,
                null as unknown as string
            )
        ).toEqual("label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(1, "label.thirdeye");

        expect(getDocumentTitle("", "", "")).toEqual("label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(2, "label.thirdeye");
    });

    it("getDocumentTitle should return appropriate document title for router breadcrumb text, invalid page title and invalid page breadcrumb text", () => {
        expect(getDocumentTitle("testRouterBreadcrumbText", "", "")).toEqual(
            "label.document-title"
        );
        expect(i18n.t).toHaveBeenNthCalledWith(1, "label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(2, "label.document-title", {
            page: "testRouterBreadcrumbText",
            app: "label.thirdeye",
        });
    });

    it("getDocumentTitle should return appropriate document title for router breadcrumb text, same page title as router breadcrumb text and invalid page breadcrumb text", () => {
        expect(
            getDocumentTitle(
                "testRouterBreadcrumbText",
                "testRouterBreadcrumbText",
                ""
            )
        ).toEqual("label.document-title");
        expect(i18n.t).toHaveBeenNthCalledWith(1, "label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(2, "label.document-title", {
            page: "testRouterBreadcrumbText",
            app: "label.thirdeye",
        });
    });

    it("getDocumentTitle should return appropriate document title for router breadcrumb text, different page title from router breadcrumb text and invalid page breadcrumb text", () => {
        expect(
            getDocumentTitle("testRouterBreadcrumbText", "testPageTitle", "")
        ).toEqual("label.document-title");
        expect(i18n.t).toHaveBeenNthCalledWith(
            1,
            "label.document-title-page-partial-router",
            { router: "testRouterBreadcrumbText", title: "testPageTitle" }
        );
        expect(i18n.t).toHaveBeenNthCalledWith(2, "label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(3, "label.document-title", {
            page: "label.document-title-page-partial-router",
            app: "label.thirdeye",
        });
    });

    it("getDocumentTitle should return appropriate document title for invalid router breadcrumb text, invalid page title and page breadcrumb text", () => {
        expect(getDocumentTitle("", "", "testPageBreadcrumbText")).toEqual(
            "label.document-title"
        );
        expect(i18n.t).toHaveBeenNthCalledWith(1, "label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(2, "label.document-title", {
            page: "testPageBreadcrumbText",
            app: "label.thirdeye",
        });
    });

    it("getDocumentTitle should return appropriate document title for invalid router breadcrumb text, page title and page breadcrumb text", () => {
        expect(
            getDocumentTitle("", "testPageTitle", "testPageBreadcrumbText")
        ).toEqual("label.document-title");
        expect(i18n.t).toHaveBeenNthCalledWith(
            1,
            "label.document-title-page-partial-page",
            { title: "testPageTitle", page: "testPageBreadcrumbText" }
        );
        expect(i18n.t).toHaveBeenNthCalledWith(2, "label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(3, "label.document-title", {
            page: "label.document-title-page-partial-page",
            app: "label.thirdeye",
        });
    });

    it("getDocumentTitle should return appropriate document title for invalid router breadcrumb text, page title and invalid page breadcrumb text", () => {
        expect(getDocumentTitle("", "testPageTitle", "")).toEqual(
            "label.document-title"
        );
        expect(i18n.t).toHaveBeenNthCalledWith(1, "label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(2, "label.document-title", {
            page: "testPageTitle",
            app: "label.thirdeye",
        });
    });

    it("getDocumentTitle should return appropriate document title for router breadcrumb text, page title and page breadcrumb text", () => {
        expect(
            getDocumentTitle(
                "testRouterBreadcrumbText",
                "testPageTitle",
                "testPageBreadcrumbText"
            )
        ).toEqual("label.document-title");
        expect(i18n.t).toHaveBeenNthCalledWith(1, "label.document-title-page", {
            router: "testRouterBreadcrumbText",
            title: "testPageTitle",
            page: "testPageBreadcrumbText",
        });
        expect(i18n.t).toHaveBeenNthCalledWith(2, "label.thirdeye");
        expect(i18n.t).toHaveBeenNthCalledWith(3, "label.document-title", {
            page: "label.document-title-page",
            app: "label.thirdeye",
        });
    });
});

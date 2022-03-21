import i18n from "i18next";

// Returns document title based on combination and availability of router breadcrumb text, page
// title and page breadcrumb text
export const getDocumentTitle = (
    routerBreadcrumbText: string,
    pageTitle: string,
    pageBreadcrumbText: string
): string => {
    let pageText = "";
    if (
        routerBreadcrumbText &&
        pageTitle &&
        routerBreadcrumbText !== pageTitle &&
        pageBreadcrumbText
    ) {
        // Everything available
        pageText = i18n.t("label.document-title-page", {
            router: routerBreadcrumbText,
            title: pageTitle,
            page: pageBreadcrumbText,
        });
    } else if (routerBreadcrumbText && !pageBreadcrumbText) {
        // Only router breadcrumb text available
        if (routerBreadcrumbText !== pageTitle && pageTitle) {
            // Both router breadcrumb text and page title can be used
            pageText = i18n.t("label.document-title-page-partial-router", {
                router: routerBreadcrumbText,
                title: pageTitle,
            });
        } else {
            // Use what's available
            pageText = pageTitle || routerBreadcrumbText;
        }
    } else if (!routerBreadcrumbText && pageBreadcrumbText) {
        // Only page breadcrumb text available
        if (pageTitle) {
            // Both page title and page breadcrumb text can be used
            pageText = i18n.t("label.document-title-page-partial-page", {
                title: pageTitle,
                page: pageBreadcrumbText,
            });
        } else {
            // Use what's available
            pageText = pageBreadcrumbText;
        }
    } else {
        // Both router breadcrumb text and page breadcrumb text not available, use what's available
        pageText = pageTitle || "";
    }

    let documentTitle = "";
    if (pageText) {
        documentTitle = i18n.t("label.document-title", {
            page: pageText,
            app: i18n.t("label.thirdeye"),
        });
    } else {
        documentTitle = i18n.t("label.thirdeye");
    }

    return documentTitle;
};

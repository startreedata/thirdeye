describe("Home Page", () => {
    it("user should navigate to appropriate page when clicked on tile buttons", () => {
        const pageHeaderTextSelector = "[data-testid='page-header-text']";
        const homePageTileButtonSelector =
            "[data-testid='home-page-tile-buttons']";

        cy.visit("/home");
        cy.get(pageHeaderTextSelector).contains("Home");

        cy.get(homePageTileButtonSelector).contains("Alerts").click();
        cy.location("pathname").should("eq", "/alerts/all");
        cy.get(pageHeaderTextSelector).contains("Alerts");

        cy.go("back");

        cy.get(homePageTileButtonSelector).contains("Anomalies").click();
        cy.location("pathname").should("eq", "/anomalies/all");
        cy.get(pageHeaderTextSelector).contains("Anomalies");

        cy.go("back");

        cy.get(homePageTileButtonSelector).contains("Configuration").click();
        cy.location("pathname").should(
            "eq",
            "/configuration/subscription-groups/all"
        );
        cy.get(pageHeaderTextSelector).contains("Configuration");

        cy.go("back");

        cy.get(homePageTileButtonSelector)
            .contains("Subscription Groups")
            .click();
        cy.location("pathname").should(
            "eq",
            "/configuration/subscription-groups/all"
        );
        cy.get(pageHeaderTextSelector).contains("Configuration");

        cy.go("back");

        cy.get(homePageTileButtonSelector).contains("Datasets").click();
        cy.location("pathname").should("eq", "/configuration/datasets/all");
        cy.get(pageHeaderTextSelector).contains("Configuration");

        cy.go("back");

        cy.get(homePageTileButtonSelector).contains("Datasources").click();
        cy.location("pathname").should("eq", "/configuration/datasources/all");
        cy.get(pageHeaderTextSelector).contains("Configuration");

        cy.go("back");

        cy.get(homePageTileButtonSelector).contains("Metrics").click();
        cy.location("pathname").should("eq", "/configuration/metrics/all");
        cy.get(pageHeaderTextSelector).contains("Configuration");
    });

    it("user should navigate to appropriate create page when clicked on create button options", () => {
        const pageHeaderActionsSelector = "[data-testid='page-header-actions']";
        const pageHeaderTextSelector = "[data-testid='page-header-text']";

        cy.visit("/home");

        cy.get(pageHeaderActionsSelector).children("button").click();
        cy.contains("Create Alert").click();
        cy.location("pathname").should("eq", "/alerts/create");
        cy.get(pageHeaderTextSelector).contains("Create Alert");

        cy.go("back");

        cy.get(pageHeaderActionsSelector).children("button").click();
        cy.contains("Create Subscription Group").click();
        cy.location("pathname").should(
            "eq",
            "/configuration/subscription-groups/create"
        );
        cy.get(pageHeaderTextSelector).contains("Create Subscription Group");

        cy.go("back");

        cy.get(pageHeaderActionsSelector).children("button").click();
        cy.contains("Create Metric").click();
        cy.location("pathname").should("eq", "/configuration/metrics/create");
        cy.get(pageHeaderTextSelector).contains("Create Metric");

        cy.go("back");

        cy.get(pageHeaderActionsSelector).children("button").click();
        cy.contains("Onboard Dataset").click();
        cy.location("pathname").should("eq", "/configuration/datasets/onboard");
        cy.get(pageHeaderTextSelector).contains("Onboard Dataset");

        cy.go("back");

        cy.get(pageHeaderActionsSelector).children("button").click();
        cy.contains("Create Datasource").click();
        cy.location("pathname").should(
            "eq",
            "/configuration/datasources/create"
        );
        cy.get(pageHeaderTextSelector).contains("Create Datasource");
    });

    it("user should navigate to appropriate page when clicked on nav bar links", () => {
        const pageHeaderTextSelector = "[data-testid='page-header-text']";

        cy.visit("/home");

        cy.get("[data-testid=nav-bar-link-home]").click();
        cy.location("pathname").should("eq", "/home");
        cy.get(pageHeaderTextSelector).contains("Home");

        cy.go("back");

        cy.get("[data-testid=nav-bar-link-alerts]").click();
        cy.location("pathname").should("eq", "/alerts/all");
        cy.get(pageHeaderTextSelector).contains("Alerts");

        cy.go("back");

        cy.get("[data-testid=nav-bar-link-anomalies]").click();
        cy.location("pathname").should("eq", "/anomalies/all");
        cy.get(pageHeaderTextSelector).contains("Anomalies");

        cy.go("back");

        cy.get("[data-testid=nav-bar-link-configuration]").click();
        cy.location("pathname").should(
            "eq",
            "/configuration/subscription-groups/all"
        );
        cy.get(pageHeaderTextSelector).contains("Configuration");
    });
});

import { render, screen } from "@testing-library/react";
import React from "react";
import { Router } from "react-router-dom";
import { appHistory } from "../../utils/history/history.util";
import { TimeRangeProvider } from "../time-range/time-range-provider/time-range-provider.component";
import { PageHeader } from "./page-header.component";

describe("Page Header", () => {
    it("should set appropriate page breadcrumbs", async () => {
        render(
            <Router history={appHistory}>
                <TimeRangeProvider>
                    <PageHeader title="Hello world" />
                </TimeRangeProvider>
            </Router>
        );

        const lol = await screen.getByText("Hello world");

        expect(lol).toBeInTheDocument();
    });
});

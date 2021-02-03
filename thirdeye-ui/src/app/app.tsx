import React, { FunctionComponent } from "react";
import { AppBar } from "./components/app-bar/app-bar.component";
import { PageContainer } from "./components/page-container/page-container.component";
import { AppRouter } from "./routers/app-router/app-router";

// ThirdEye UI app
export const App: FunctionComponent = () => {
    return (
        <>
            <AppBar />

            <PageContainer>
                <AppRouter />
            </PageContainer>
        </>
    );
};

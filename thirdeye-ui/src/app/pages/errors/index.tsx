import React from "react";
import { MainLayout } from "../../platform/components/layout/main/page.component";
import { ErrorHeader } from "./sections/header";
import { ErrorTable } from "./sections/error-table";

export const ErrorListPage = (): JSX.Element => {
    return (
        <MainLayout>
            <ErrorHeader />
            <ErrorTable />
        </MainLayout>
    );
};

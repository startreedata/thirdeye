// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute } from "../../utils/routes/routes.util";

const LoginPage = lazy(() =>
    import(
        /* webpackChunkName: "login-page" */ "../../pages/login-page/login-page.component"
    ).then((module) => ({ default: module.LoginPage }))
);

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Login path */}
                <Route element={<LoginPage />} path={AppRoute.LOGIN} />

                {/* No match found, redirect to login path */}
                <Route
                    element={<Navigate replace to={AppRoute.LOGIN} />}
                    path="*"
                />
            </Routes>
        </Suspense>
    );
};

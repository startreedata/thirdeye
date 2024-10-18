/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React from "react";
import SwaggerUI, { SwaggerUIProps } from "swagger-ui-react";
import "swagger-ui-react/swagger-ui.css";
import { useAuthV1 } from "../../platform/stores/auth-v1/auth-v1.store";

export const SwaggerDocs = (): JSX.Element => {
    const [workspace] = useAuthV1((state) => [state.workspace]);
    const authData = localStorage.getItem("auth-v1");
    const accessToken = authData && JSON.parse(authData!).state.accessToken;

    const swaggerOptions: SwaggerUIProps = {
        requestInterceptor: (req) => {
            if (accessToken) {
                req.headers["Authorization"] = `Bearer ${accessToken}`;
            }
            if (workspace.id) {
                req.headers["namespace"] = workspace.id;
            }

            return req;
        },
        onComplete: (): void => {
            const authSection = document.querySelector(".scheme-container");
            authSection?.remove();
        },
    };

    return (
        <div>
            <SwaggerUI
                docExpansion="none"
                url="/openapi.json"
                {...swaggerOptions}
            />
        </div>
    );
};

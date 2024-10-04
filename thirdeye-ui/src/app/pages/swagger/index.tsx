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
            const authButton = document.querySelector(".auth-wrapper > button");
            authButton?.addEventListener("click", (): void => {
                setTimeout((): void => {
                    const inputs = document.querySelectorAll<HTMLInputElement>(
                        ".modal-ux-content input"
                    );
                    if (workspace.id) {
                        inputs[0].value = workspace.id;
                    }
                    if (accessToken) {
                        inputs[1].value = `Bearer ${accessToken}`;
                    }
                });
            });
        },
    };

    return (
        <div>
            <SwaggerUI url="/openapi.json" {...swaggerOptions} />
        </div>
    );
};

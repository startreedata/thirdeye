/*
 * Copyright 2023 StarTree Inc
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
/*
 * Copyright 2023 StarTree Inc.
 *
 * All rights reserved. Confidential and proprietary information of StarTree Inc.
 */
import * as Sentry from "@sentry/react";
import { useQuery } from "@tanstack/react-query";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useAuthProviderV1 } from "../../platform/components";
import { getAppConfiguration as getAppConfigurationRest } from "../../rest/app-config/app-config.rest";
import { getThirdEyeUiVersion } from "../../utils/version/version.util";
import { AnalyticsAndErrorReportingProviderV1Props } from "./analytics-and-error-reporting-provider-v1.interfaces";

const getEnvironmentFromHostname = (hostname: string): string => {
    return hostname.split(".").slice(1).join(".") || "production";
};

export const AnalyticsAndErrorReportingProviderV1: FunctionComponent<AnalyticsAndErrorReportingProviderV1Props> =
    ({ children }) => {
        const [isSentrySetup, setIsSentrySetup] = useState(false);
        const { data: appConfig } = useQuery({
            queryKey: ["appConfig"],
            queryFn: getAppConfigurationRest,
        });
        const { authUser } = useAuthProviderV1();

        // Provides tracking script to inject
        const getTrackingScript = (
            id: string | number,
            userEmail: string,
            userName: string
        ): string => {
            let trackingScript =
                `window.heap=window.heap||[],heap.load=function(e,t){window.heap.appid=e,window.heap.config=t=t||{};` +
                `var r=document.createElement("script");r.type="text/javascript",r.async=!0,r.src="https://cdn.heapanalytics.com/js/heap-"+e+".js";` +
                `var a=document.getElementsByTagName("script")[0];a.parentNode.insertBefore(r,a);` +
                `for(var n=function(e){return function(){heap.push([e].concat(Array.prototype.slice.call(arguments,0)))}},` +
                `p=["addEventProperties","addUserProperties","clearEventProperties","identify","resetIdentity","removeEventProperty","setEventProperties","track","unsetEventProperty"],` +
                `o=0;o<p.length;o++)heap[p[o]]=n(p[o])};` +
                `heap.load("${id}");`;

            if (userEmail) {
                trackingScript += `heap.identify("${userEmail}");heap.addUserProperties({name:"${userName}",email:"${userEmail}",})`;
            }

            return trackingScript;
        };

        // Initializes tracking
        const initializeTracking = (appId: string): void => {
            if (!document || !document.head) {
                return;
            }

            const trackingScript = document.createElement("script");
            trackingScript.innerHTML = getTrackingScript(
                appId,
                authUser.email,
                authUser.name
            );
            trackingScript.async = true;

            document.head.appendChild(trackingScript);
        };

        useEffect(() => {
            if (!authUser?.email || !appConfig?.heap?.environmentId) {
                return;
            }

            initializeTracking(appConfig.heap.environmentId);
        }, [authUser, appConfig]);

        useEffect(() => {
            if (window.location.host.includes("localhost:7004")) {
                return;
            }

            if (appConfig?.sentry?.clientDsn && !isSentrySetup) {
                Sentry.init({
                    environment: getEnvironmentFromHostname(
                        window.location.hostname
                    ),
                    release: getThirdEyeUiVersion(),
                    dsn: appConfig.sentry.clientDsn,
                    integrations: [
                        new Sentry.BrowserTracing({
                            tracePropagationTargets: [],
                        }),
                    ],
                    tracesSampleRate: 0.25,
                });
                if (authUser?.email) {
                    Sentry.setUser({ email: authUser.email });
                }
                setIsSentrySetup(true);

                return;
            }
        }, [appConfig]);

        return <>{children}</>;
    };

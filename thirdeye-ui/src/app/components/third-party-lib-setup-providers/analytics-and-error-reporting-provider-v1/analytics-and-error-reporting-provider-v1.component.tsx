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
import React, { FunctionComponent, useEffect, useState } from "react";
import { useAuthProviderV1 } from "../../../platform/components";
import { getAppConfiguration as getAppConfigurationRest } from "../../../rest/app-config/app-config.rest";
import { getThirdEyeUiVersion } from "../../../utils/version/version.util";
import { IntercomProvider } from "../intercom-provider/intercom-provider.component";
import { AnalyticsAndErrorReportingProviderV1Props } from "./analytics-and-error-reporting-provider-v1.interfaces";
import { useFetchQuery } from "../../../rest/hooks/useFetchQuery";

const getEnvironmentFromHostname = (hostname: string): string => {
    return hostname.split(".").slice(1).join(".") || "production";
};

export const AnalyticsAndErrorReportingProviderV1: FunctionComponent<AnalyticsAndErrorReportingProviderV1Props> =
    ({ children }) => {
        const [isSentrySetup, setIsSentrySetup] = useState(false);
        const { data: appConfig } = useFetchQuery({
            queryKey: ["appConfig"],
            queryFn: getAppConfigurationRest,
        });
        const { authUser } = useAuthProviderV1();

        const [isScriptInjected, setIsScriptInjected] = useState(false);
        const [isUserSetup, setIsUserSetup] = useState(false);

        // Provides tracking script to inject
        const generateTrackingScriptContent = (id: string | number): string => {
            return (
                `window.heap=window.heap||[],heap.load=function(e,t){window.heap.appid=e,window.heap.config=t=t||{};` +
                `var r=document.createElement("script");r.type="text/javascript",r.async=!0,r.src="https://cdn.heapanalytics.com/js/heap-"+e+".js";` +
                `var a=document.getElementsByTagName("script")[0];a.parentNode.insertBefore(r,a);` +
                `for(var n=function(e){return function(){heap.push([e].concat(Array.prototype.slice.call(arguments,0)))}},` +
                `p=["addEventProperties","addUserProperties","clearEventProperties","identify","resetIdentity","removeEventProperty","setEventProperties","track","unsetEventProperty"],` +
                `o=0;o<p.length;o++)heap[p[o]]=n(p[o])};` +
                `heap.load("${id}", {secureCookie: true});`
            );
        };

        const generateUserSetupScriptContent = (
            userEmail: string,
            userName: string
        ): string => {
            return `heap.identify("${userEmail}");heap.addUserProperties({name:"${userName}",email:"${userEmail}",})`;
        };

        // Initializes tracking
        useEffect(() => {
            if (!appConfig?.heap?.environmentId || isScriptInjected) {
                return;
            }

            if (!document || !document.head) {
                return;
            }

            const trackingScript = document.createElement("script");
            trackingScript.innerHTML = generateTrackingScriptContent(
                appConfig?.heap?.environmentId
            );
            trackingScript.defer = true;

            document.head.appendChild(trackingScript);
            setIsScriptInjected(true);
        }, [appConfig]);

        useEffect(() => {
            // Only set up when heap script is injected
            if (isUserSetup || !isScriptInjected) {
                return;
            }

            if (!authUser?.email) {
                return;
            }

            const trackingScript = document.createElement("script");
            trackingScript.innerHTML = generateUserSetupScriptContent(
                authUser?.email,
                authUser?.name
            );
            trackingScript.defer = true;

            document.head.appendChild(trackingScript);
            setIsUserSetup(true);
        }, [isScriptInjected, authUser]);

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

        return (
            <IntercomProvider
                appId={appConfig?.intercom?.appId}
                email={authUser.email}
                nameOfUser={authUser.name}
            >
                {children}
            </IntercomProvider>
        );
    };

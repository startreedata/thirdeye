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
import React, { FunctionComponent, useEffect, useState } from "react";
import { IntercomProviderProps } from "./intercom-provider.interfaces";

export const IntercomProvider: FunctionComponent<IntercomProviderProps> = ({
    children,
    appId,
    nameOfUser,
    email,
}) => {
    const [isSetup, setIsSetup] = useState(false);

    // Provides chat initializer script to inject
    const getInitialScriptToInsert = (
        id: string,
        userEmail: string,
        userName: string
    ): string => {
        let scriptToInsert =
            `(function(){var w=window;var ic=w.Intercom;if(typeof ic==="function"){ic('reattach_activator');ic('update',w.intercomSettings);}` +
            `else{var d=document;var i=function(){i.c(arguments);};i.q=[];i.c=function(args){i.q.push(args);};w.Intercom=i;` +
            `var l=function(){var s=d.createElement('script');s.type='text/javascript';s.async=true;` +
            `s.src='https://widget.intercom.io/widget/${id}';var x=d.getElementsByTagName('script')[0];` +
            `x.parentNode.insertBefore(s,x);};if(document.readyState==='complete'){l();}` +
            `else if(w.attachEvent){w.attachEvent('onload',l);}else{w.addEventListener('load',l,false);}}})();`;

        if (userEmail) {
            scriptToInsert += `
                        window.Intercom("boot", {
                            api_base: "https://api-iam.intercom.io",
                            app_id: "${id}",
                            email: "${userEmail}",
                            name: "${userName}",
                            user_id: "${userEmail}"
                        })
                    `;
        }

        return scriptToInsert;
    };

    // Initializes script
    const initializeScript = (): void => {
        if (!document || !document.head) {
            return;
        }

        if (!appId || !nameOfUser || !email) {
            return;
        }

        const scriptToInsert = document.createElement("script");
        scriptToInsert.innerHTML = getInitialScriptToInsert(
            appId,
            email,
            nameOfUser
        );
        scriptToInsert.async = true;

        document.head.appendChild(scriptToInsert);
    };

    useEffect(() => {
        if (!appId || isSetup || !nameOfUser || !email) {
            return;
        }

        initializeScript();
        setIsSetup(true);
    }, [appId, nameOfUser, email]);

    return <>{children}</>;
};

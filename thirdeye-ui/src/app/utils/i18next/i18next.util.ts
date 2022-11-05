/*
 * Copyright 2022 StarTree Inc
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
import { InitOptions } from "i18next";
import enUS from "../../locale/languages/en-us.json";

// Returns i18next options
export const getInitOptions = (): InitOptions => {
    return {
        supportedLngs: ["en-US"],
        resources: {
            "en-US": { translation: enUS },
        },
        fallbackLng: ["en-US"],
        interpolation: {
            escapeValue: false, // XSS safety provided by React
        },
        missingKeyHandler: (_lngs, _ns, key) =>
            console.error(`i18next: key not found "${key}"`),
        saveMissing: true, // Required for missing key handler
    };
};

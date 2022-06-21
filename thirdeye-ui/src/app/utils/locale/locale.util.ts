///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import { Settings } from "luxon";
import numbro from "numbro";
import { initReactI18next } from "react-i18next";
import { getInitOptions } from "../i18next/i18next.util";

export const initLocale = (): void => {
    // Initialize i18next (language)
    i18n.use(LanguageDetector) // Detects system language
        .use(initReactI18next)
        .init(getInitOptions(), () => {
            // Force numbro (number) and Luxon (date/time) to use the same language
            numbro.setLanguage(i18n.language);
            Settings.defaultLocale = i18n.language;
        });
};

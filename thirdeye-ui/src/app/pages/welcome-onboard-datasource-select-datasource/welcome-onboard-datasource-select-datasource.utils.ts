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
import type { DefaultNamespace, UseTranslationResponse } from "react-i18next";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import type {
    DatasourceOption,
    DatasourceOptionGroups,
} from "./welcome-onboard-datasource-select-datasource.interfaces";

export const STARTREE_CLOUD = "startree-cloud";
export const OTHER_SOURCES = "other-sources";
export const ADD_NEW_DATASOURCE = "##__add-new-datasource__##";

export const getDatasources = (
    datasources: Datasource[] = [],
    t: UseTranslationResponse<DefaultNamespace>["t"]
): Readonly<DatasourceOption[]> => {
    return [
        ...((datasources || []).map((d) => ({
            label: d.name,
            value: d.name,
            sourceType: STARTREE_CLOUD,
        })) as DatasourceOption[]),
        {
            label: t("message.add-new-pinot-datasource"),
            value: ADD_NEW_DATASOURCE,
            sourceType: OTHER_SOURCES,
        },
    ] as const;
};

export const getDatasourceGroups = (
    datasources: Datasource[] = [],
    t: UseTranslationResponse<DefaultNamespace>["t"]
): DatasourceOptionGroups[] => {
    const DATASOURCES = getDatasources(datasources || [], t);

    return [
        {
            key: "cloud",
            title: t("message.startree-cloud-sources"),
            options: DATASOURCES.filter(
                ({ sourceType }) => sourceType === "startree-cloud"
            ),
        },
        {
            key: "other",
            title: t("message.other-sources"),
            options: DATASOURCES.filter(
                ({ sourceType }) => sourceType === "other-sources"
            ),
        },
    ];
};

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
import { Datasource } from "../../rest/dto/datasource.interfaces";
import type {
    DatasourceOption,
    DatasourceOptionGroups,
} from "./welcome-onboard-datasource-select-datasource.interfaces";

export const STARTREE_CLOUD = "startree-cloud";
export const OTHER_SOURCES = "other-sources";
export const ADD_NEW_DATASOURCE = "add-new-datasource";

export const getDatasources = (
    datasources: Datasource[] = []
): Readonly<DatasourceOption[]> => {
    return [
        ...((datasources || []).map((d) => ({
            label: d.name,
            value: d.id,
            sourceType: STARTREE_CLOUD,
        })) as DatasourceOption[]),

        // TODO: Remove
        // {
        //     label: "BYOC_production_ENV",
        //     value: "BYOC_production_ENV",
        //     sourceType: "startree-cloud",
        // },
        // {
        //     label: "sales_Pinot_Table",
        //     value: "sales_Pinot_Table",
        //     sourceType: "startree-cloud",
        // },
        {
            label: "Add new Pinot datasource",
            value: ADD_NEW_DATASOURCE,
            sourceType: OTHER_SOURCES,
        },
    ] as const;
};

export const getDatasourceGroups = (
    datasources: Datasource[] = []
): DatasourceOptionGroups[] => {
    const DATASOURCES = getDatasources(datasources || []);

    return [
        {
            key: "cloud",
            title: "StarTree Cloud sources",
            options: DATASOURCES.filter(
                ({ sourceType }) => sourceType === "startree-cloud"
            ),
        },
        {
            key: "other",
            title: "Other sources",
            options: DATASOURCES.filter(
                ({ sourceType }) => sourceType === "other-sources"
            ),
        },
    ];
};

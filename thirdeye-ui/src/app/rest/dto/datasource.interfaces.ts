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
export interface Datasource {
    id?: number;
    name: string;
    type: string;
    properties: DatasourceProperties;
    metaList?: DatasourceMetaList[];
    defaultQueryOptions?: { [key: string]: string };
}

export interface DatasourceProperties {
    zookeeperUrl: string;
    clusterName: string;
    controllerConnectionScheme: string;
    controllerHost: string;
    controllerPort: number;
    cacheLoaderClassName: string;
    brokerUrl?: string;
}

export interface DatasourceMetaList {
    classRef: string;
    properties: { [index: string]: unknown };
}

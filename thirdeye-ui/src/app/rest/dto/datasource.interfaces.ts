export interface Datasource {
    id: number;
    name: string;
    type: string;
    properties: DatasourceProperties;
    metaList: DatasourceMetaList[];
}

export interface DatasourceProperties {
    zooKeeperURL: string;
    clusterName: string;
    controllerConnectionScheme: string;
    controllerHost: string;
    controllerPort: number;
    cacheLoaderClassName: string;
}

export interface DatasourceMetaList {
    classRef: string;
    properties: { [index: string]: unknown };
}

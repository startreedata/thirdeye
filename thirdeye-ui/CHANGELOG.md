## [2.19.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.18.0...thirdeye-ui-2.19.0) (2023-05-24)


### Features

* [TE-1497](https://cortexdata.atlassian.net/browse/TE-1497) deleting datasource and dataset now deletes metrics and datasets ([6a58f5466](https://github.com/startreedata/thirdeye/commit/6a58f5466a469b074e80c58b7bb54d836f6365ad))
* [TE-1518](https://cortexdata.atlassian.net/browse/TE-1518) add json editor button to create alert setup threshold page ([60c462535](https://github.com/startreedata/thirdeye/commit/60c462535129f2f1db6b7f4f6a891fd379419bc9))


### Bug Fixes

* [TE-1518](https://cortexdata.atlassian.net/browse/TE-1518) fix bug introduced where old values being used for alert config ([bc2aac30a](https://github.com/startreedata/thirdeye/commit/bc2aac30a27f7b3bdae1df0b06b74362fc7d4577))
* [TE-1587](https://cortexdata.atlassian.net/browse/TE-1587) escape button closes dialog not take to alert edit page ([be07bf9b0](https://github.com/startreedata/thirdeye/commit/be07bf9b03e236bdba37c37bebff798b409a9804))

## [2.18.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.17.1...thirdeye-ui-2.18.0) (2023-04-24)


### Features

* [TE-1438](https://cortexdata.atlassian.net/browse/TE-1438) add `resetData` function to all actions ([83fa539c7](https://github.com/startreedata/thirdeye/commit/83fa539c7fdd2d7dd5354a8ecc49fb314e2e14da))
* [TE-1457](https://cortexdata.atlassian.net/browse/TE-1457) add edit link in modal in alert view ([fe8903fe5](https://github.com/startreedata/thirdeye/commit/fe8903fe5f056f3059b6a5d0a26c1bc8c2d665f5))
* [TE-1494](https://cortexdata.atlassian.net/browse/TE-1494) hide time for daily granularity alerts ([3fd562986](https://github.com/startreedata/thirdeye/commit/3fd562986064e6ad61e2f91e4d6e21a757ece988))
* [TE-1513](https://cortexdata.atlassian.net/browse/TE-1513) use insights api during cohort detection to better default selected dates ([8e2405cd8](https://github.com/startreedata/thirdeye/commit/8e2405cd88a37d937e94acabf7987fd9c5138a90))


### Bug Fixes

* [TE-1438](https://cortexdata.atlassian.net/browse/TE-1438) delete anomalies from chart when alert is reset ([4ad65571e](https://github.com/startreedata/thirdeye/commit/4ad65571eb96957afddc42bce07f9236d824a973))
* [TE-1496](https://cortexdata.atlassian.net/browse/TE-1496) auto-open notifications if subscription groups are selected ([8cebff6b2](https://github.com/startreedata/thirdeye/commit/8cebff6b2140c440d957915623f1f5b6d34fc149))
* [TE-1496](https://cortexdata.atlassian.net/browse/TE-1496) do not autoselect all dimensions in cohort detector ([147a88457](https://github.com/startreedata/thirdeye/commit/147a884570b16aa7552254829ebf2e65c8bc4ad1))
* [TE-1496](https://cortexdata.atlassian.net/browse/TE-1496) show all subscription groups for alert in all alerts page ([f3c391591](https://github.com/startreedata/thirdeye/commit/f3c391591a3eaa899d7b48cf0ea418aa65a02d5a))
* [TE-1499](https://cortexdata.atlassian.net/browse/TE-1499) default lookback 28D, alerts sorted by created, create button changes ([fc2b8ab6b](https://github.com/startreedata/thirdeye/commit/fc2b8ab6b49afa38e8f775e2b87d1cd157d607e9))

### [2.17.1](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.17.0...thirdeye-ui-2.17.1) (2023-04-18)


### Bug Fixes

* [TE-1507](https://cortexdata.atlassian.net/browse/TE-1507) account for missing `notificationSchemes` field ([f5e041c19](https://github.com/startreedata/thirdeye/commit/f5e041c1955ea0dba2afbb33e0d5e0a37fdcfa59))

## [2.17.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.16.1...thirdeye-ui-2.17.0) (2023-04-17)


### Features

* [TE-1471](https://cortexdata.atlassian.net/browse/TE-1471) anomalies details v2 ([d3e480fef](https://github.com/startreedata/thirdeye/commit/d3e480fef2c52bc999e3ae87720ceb1eb28ae159))
* [TE-1471](https://cortexdata.atlassian.net/browse/TE-1471) anomaly feedback component changes for anomaly details page ([824b2c2df](https://github.com/startreedata/thirdeye/commit/824b2c2df5213fff679fd02e37a98397da8cf3ea))
* [TE-1471](https://cortexdata.atlassian.net/browse/TE-1471) anomaly view chart and page skeleton component ([423c10f82](https://github.com/startreedata/thirdeye/commit/423c10f82eaf4893df58d5dfcc0a1aa80d318bbd))
* [TE-1471](https://cortexdata.atlassian.net/browse/TE-1471) past duration picker ([4dba98a23](https://github.com/startreedata/thirdeye/commit/4dba98a23c38dddd33d8b3fad368988827a0cbfd))
* [TE-1500](https://cortexdata.atlassian.net/browse/TE-1500) less clicks to add alert in subscription group edit flow ([594f3e7f3](https://github.com/startreedata/thirdeye/commit/594f3e7f30736d3721b65fdf297429f155cb9d25))

### [2.16.1](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.16.0...thirdeye-ui-2.16.1) (2023-04-11)


### Bug Fixes

* [TE-1495](https://cortexdata.atlassian.net/browse/TE-1495) fix bug where alert name was not being passed to POST ([b16958550](https://github.com/startreedata/thirdeye/commit/b169585509e20a6dcf74359ff73a4a7827f67c69))

## [2.16.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.15.1...thirdeye-ui-2.16.0) (2023-04-11)


### Features

* [TE-1449](https://cortexdata.atlassian.net/browse/TE-1449) add json and advanced editor to the welcome create alert flow ([833d4cba1](https://github.com/startreedata/thirdeye/commit/833d4cba15bbaf0d385f3176cf2ce727a484d145))
* [TE-1449](https://cortexdata.atlassian.net/browse/TE-1449) add page switch to welcome create alert flow ([2aff713e1](https://github.com/startreedata/thirdeye/commit/2aff713e1dd5e7b50bc1147f509380eb37ecd5c1))
* [TE-1456](https://cortexdata.atlassian.net/browse/TE-1456) support monitoringGranularity, seasonalityPeriod, and lookback ([ed192bed4](https://github.com/startreedata/thirdeye/commit/ed192bed4a9ad0b1f9cdef695de3e6330ba8b1e9))
* [TE-1492](https://cortexdata.atlassian.net/browse/TE-1492) add link to recipe for ads monitoring sample alert ([1c25ca5dc](https://github.com/startreedata/thirdeye/commit/1c25ca5dc6f6b843f89ba59f3747abeecea5cd3d))


### Bug Fixes

* [TE-1439](https://cortexdata.atlassian.net/browse/TE-1439) make rcaAggregationFunction optional for JSON editor only ([fcf323d40](https://github.com/startreedata/thirdeye/commit/fcf323d40ca44ed01ddc0d9865ee1e899b695f76))
* [TE-1451](https://cortexdata.atlassian.net/browse/TE-1451) boolean type field take show state of default value if no value set ([1f14df73b](https://github.com/startreedata/thirdeye/commit/1f14df73bd329aeef6d829b5f6e3f1b666e2c5b8))

### [2.15.1](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.15.0...thirdeye-ui-2.15.1) (2023-04-03)


### Bug Fixes

* [TE-1449](https://cortexdata.atlassian.net/browse/TE-1449) cosmetic fix to padding on left and right sides with loading switch component ([caa2bcab2](https://github.com/startreedata/thirdeye/commit/caa2bcab29c582af9f54320a5210db5e2f928701))

## [2.15.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.14.0...thirdeye-ui-2.15.0) (2023-03-31)


### Features

* [TE-1437](https://cortexdata.atlassian.net/browse/TE-1437) move switch to different create pages and links to alert template documentation ([fd2851001](https://github.com/startreedata/thirdeye/commit/fd2851001bc93da1680ec8466eb4a8e795543bac))

## [2.14.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.13.0...thirdeye-ui-2.14.0) (2023-03-29)


### Features

* [TE-1441](https://cortexdata.atlassian.net/browse/TE-1441) remove 2.13.1 entry in changelog and forcing new version ([28808d5bd](https://github.com/startreedata/thirdeye/commit/28808d5bdeaef12946276de8e1423d5495206a00))


### Bug Fixes

* [TE-1399](https://cortexdata.atlassian.net/browse/TE-1399) fix canceled apis when using filters for anomalies ([390949e3e](https://github.com/startreedata/thirdeye/commit/390949e3e564d3135f9cb05b7fc9b24a4df36b1f))
* [TE-1412](https://cortexdata.atlassian.net/browse/TE-1412) use insights api to derive timezone for anomaly chart ([57770c9b1](https://github.com/startreedata/thirdeye/commit/57770c9b170e13d603b9f15470d2d5d61352d639))

## [2.13.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.12.0...thirdeye-ui-2.13.0) (2023-03-20)


### Features

* [TE-1386](https://cortexdata.atlassian.net/browse/TE-1386) updated sidebar logos ([3d194460e](https://github.com/startreedata/thirdeye/commit/3d194460e662cc7ac0d88d1f9a9d381efe2693cb))
* [TE-1388](https://cortexdata.atlassian.net/browse/TE-1388) enabling drag select to assign daterange to anomalies in create anomaly page ([a1df2f101](https://github.com/startreedata/thirdeye/commit/a1df2f1014f9aed32dbe8440132d330df1853439))


### Bug Fixes

* [TE-1398](https://cortexdata.atlassian.net/browse/TE-1398) fixed embed video link in on welcome page ([c515e867a](https://github.com/startreedata/thirdeye/commit/c515e867a9d8eb6608cdb56329a4c25e87d967c7))

## [2.12.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.11.0...thirdeye-ui-2.12.0) (2023-03-13)


### Features

* [TE-1361](https://cortexdata.atlassian.net/browse/TE-1361) remove holt-winters from guided user flow algorithm selection ([dea706e01](https://github.com/startreedata/thirdeye/commit/dea706e01ed527848901671755531f4938d1f9be))
* [TE-1377](https://cortexdata.atlassian.net/browse/TE-1377) add rideshare sample alert ([d64e72f44](https://github.com/startreedata/thirdeye/commit/d64e72f44bab0453c4ed85fa590f3778755a8aca))
* [TE-1378](https://cortexdata.atlassian.net/browse/TE-1378) enable callback function for when users make a range selection in TimeSeriesChart ([59580a914](https://github.com/startreedata/thirdeye/commit/59580a91492e9726bff2476218da3bb0bbc496be))
* [TE-840](https://cortexdata.atlassian.net/browse/TE-840) report missed anomalies ([c634563b1](https://github.com/startreedata/thirdeye/commit/c634563b14dd1f4122e8d551f4f7b6f077bc9dfe))

## [2.11.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.10.0...thirdeye-ui-2.11.0) (2023-03-03)


### Features

* [TE-1206](https://cortexdata.atlassian.net/browse/TE-1206) cancel api calls when user leaves some pages ([31e0834cc](https://github.com/startreedata/thirdeye/commit/31e0834ccc429f25e9586ed65da347d3034393cb))
* [TE-1266](https://cortexdata.atlassian.net/browse/TE-1266) improve video tutorial experience ([91e243ed2](https://github.com/startreedata/thirdeye/commit/91e243ed29e2f4d83400d27655f31156b786a3ae))
* [TE-1298](https://cortexdata.atlassian.net/browse/TE-1298) anomaly filtering by feedback of not an anomaly ([760ed0c04](https://github.com/startreedata/thirdeye/commit/760ed0c048501d523a15d08c0661e3c4a700aa89))
* [TE-1304](https://cortexdata.atlassian.net/browse/TE-1304) support count * as a metric ([7be582904](https://github.com/startreedata/thirdeye/commit/7be582904abe99764fa9eb2e06e80776da98e342))
* [TE-1305](https://cortexdata.atlassian.net/browse/TE-1305) added link to config section in welcome flow ([b204170c5](https://github.com/startreedata/thirdeye/commit/b204170c51069f6fe6ebea8215eb2b7526acb180))
* [TE-1317](https://cortexdata.atlassian.net/browse/TE-1317) added `aggregationColumn` to new alert configuration ([a79f9c248](https://github.com/startreedata/thirdeye/commit/a79f9c248506d34420d08bc62eca2a61e254b7d7))
* [TE-1364](https://cortexdata.atlassian.net/browse/TE-1364) added sequential flow between tabs in subscription group wizard ([ec65c2c80](https://github.com/startreedata/thirdeye/commit/ec65c2c802362cdd3c72d47e3bb191bb927f7216))
* [TE-1373](https://cortexdata.atlassian.net/browse/TE-1373) changed subscription group routing to match the rest of the app ([7f68b9c90](https://github.com/startreedata/thirdeye/commit/7f68b9c9009460fcdb79b426ee3650986ed3449c))


### Bug Fixes

* [TE-1366](https://cortexdata.atlassian.net/browse/TE-1366) allow dimension exploration values in template properties validator ([f2aff1ee7](https://github.com/startreedata/thirdeye/commit/f2aff1ee7fa7689c987d5f52f77325becc861baf))

## [2.10.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.9.1...thirdeye-ui-2.10.0) (2023-02-27)


### Features

* [TE-1142](https://cortexdata.atlassian.net/browse/TE-1142) anomalies filtering in alert create for new user flow ([2a3ce381a](https://github.com/startreedata/thirdeye/commit/2a3ce381a73238e7f1162a159b02b83d25c78937))
* [TE-1270](https://cortexdata.atlassian.net/browse/TE-1270) alert notification fatigue revamp ([cc2b275b2](https://github.com/startreedata/thirdeye/commit/cc2b275b2c371d0ababf5ee30bd26cdb597dbd7e))
* [TE-1270](https://cortexdata.atlassian.net/browse/TE-1270) remove misplaced character from UI ([b6ec36281](https://github.com/startreedata/thirdeye/commit/b6ec3628169d26726ca2b2152d7fa83d9293ef62))


### Bug Fixes

* [TE-1142](https://cortexdata.atlassian.net/browse/TE-1142) fix bug where page would navigate back to filter page ([75cd444fc](https://github.com/startreedata/thirdeye/commit/75cd444fc9e48eff4eb5e2d9f28910143f7a10d3))
* [TE-1356](https://cortexdata.atlassian.net/browse/TE-1356) use evaluation.alert.template in preview charts when for timezone ([6ff2951a7](https://github.com/startreedata/thirdeye/commit/6ff2951a78c7d647d102f0dcd1b3401570073a6c))
* [TE-1357](https://cortexdata.atlassian.net/browse/TE-1357) revert default svg of time series chart and make only preview charts use it ([60d7759e9](https://github.com/startreedata/thirdeye/commit/60d7759e9257b49b11d2d28b35995bc1015db766))

### [2.9.1](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.9.0...thirdeye-ui-2.9.1) (2023-02-16)


### Bug Fixes

* [TE-1331](https://cortexdata.atlassian.net/browse/TE-1331) add missing non number filter on upper and lower bound ([9b4d41a4e](https://github.com/startreedata/thirdeye/commit/9b4d41a4ea4f67a159e5f4219dce32074533f862))

## [2.9.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.8.0...thirdeye-ui-2.9.0) (2023-02-09)


### Features

* [TE-1180](https://cortexdata.atlassian.net/browse/TE-1180) validate alert templateProperties based on properties content ([8342d0af0](https://github.com/startreedata/thirdeye/commit/8342d0af0c042020cfeaa25d0c2d7c246586cedc))
* [TE-1231](https://cortexdata.atlassian.net/browse/TE-1231) have the chart timeframe adapt to the baseline offset ([3c3efbd31](https://github.com/startreedata/thirdeye/commit/3c3efbd310fb8e85905815362d3daa671c49fcda))
* [TE-1267](https://cortexdata.atlassian.net/browse/TE-1267) updated tooltips on welcome flow ([662db4332](https://github.com/startreedata/thirdeye/commit/662db433297ec866ea17c681c4a77176a8f6d5aa))

## [2.8.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.7.0...thirdeye-ui-2.8.0) (2023-02-06)


### Features

* [TE-1180](https://cortexdata.atlassian.net/browse/TE-1180) use specific components depending on alert template property type ([5b1d5f4ae](https://github.com/startreedata/thirdeye/commit/5b1d5f4ae4ec3556006d52e1a2f92ed8edbf661f))
* [TE-1216](https://cortexdata.atlassian.net/browse/TE-1216) create alert flow support timezone ([2786d6e85](https://github.com/startreedata/thirdeye/commit/2786d6e85465a9f998dd4daf311921a9cba28a86))
* [TE-1216](https://cortexdata.atlassian.net/browse/TE-1216) support timezone in alert details view pages ([a2bc53cac](https://github.com/startreedata/thirdeye/commit/a2bc53caceb933bd559f58e3062caefac4581208))
* [TE-1233](https://cortexdata.atlassian.net/browse/TE-1233) support auto refreshing when user resets an anomaly in alerts details page ([7138b6e1d](https://github.com/startreedata/thirdeye/commit/7138b6e1d3b0b5213418534cb26873e62cbe33fd))
* [TE-1239](https://cortexdata.atlassian.net/browse/TE-1239) disable update button for alert until user previews changes ([7b401b82f](https://github.com/startreedata/thirdeye/commit/7b401b82fecfe6eefe9f1b99210bfdf554d9cd00))
* [TE-1284](https://cortexdata.atlassian.net/browse/TE-1284) added oidc "groups" claim to dex default scopes in platform code in thirdeye-ui ([7331ce07c](https://github.com/startreedata/thirdeye/commit/7331ce07c44ef8d3921403fc8f055dc98c2df1e0))


### Bug Fixes

* [TE-1233](https://cortexdata.atlassian.net/browse/TE-1233) fix defect where notification indicating auto reload would not go away ([46d4013df](https://github.com/startreedata/thirdeye/commit/46d4013df05199a287477c77dfe2805374b7247f))

## [2.7.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.6.0...thirdeye-ui-2.7.0) (2023-01-30)


### Features

* [TE-1207](https://cortexdata.atlassian.net/browse/TE-1207) added exponential notation and Infinite label to very large anomaly deviations ([71d20c703](https://github.com/startreedata/thirdeye/commit/71d20c703b2992c1cd44070a0cafe52a000f4bb7))
* [TE-1209](https://cortexdata.atlassian.net/browse/TE-1209) improved contrast on link in markdown tooltip ([155c52d58](https://github.com/startreedata/thirdeye/commit/155c52d58da3025081e3c0e1d395e8e01f8bee99))
* [TE-1216](https://cortexdata.atlassian.net/browse/TE-1216) charting and determining timezone util functions for support for timezone ([a9200a64e](https://github.com/startreedata/thirdeye/commit/a9200a64ef981b5fb6a5cceaabdc42da12ef72cd))
* [TE-1216](https://cortexdata.atlassian.net/browse/TE-1216) enhance time series chart to support displaying times by different timezones ([0612e5b1d](https://github.com/startreedata/thirdeye/commit/0612e5b1d9184a6eda8c5449eee1d69237ee21e6))
* [TE-1216](https://cortexdata.atlassian.net/browse/TE-1216) enhance timepicker to support displaying and returning selected dates in timezone ([81359917f](https://github.com/startreedata/thirdeye/commit/81359917f079102bfff5debdb2a142fca291e959))
* [TE-1221](https://cortexdata.atlassian.net/browse/TE-1221) improved ui for top contributors in investigate anomalies ([d093e3f2a](https://github.com/startreedata/thirdeye/commit/d093e3f2abec71103763f709a8cf39244b494384))
* [TE-1225](https://cortexdata.atlassian.net/browse/TE-1225) make links in anomaly table more intuitive ([34a259bdf](https://github.com/startreedata/thirdeye/commit/34a259bdf6f51945460c64f4ca75ee7a0d4f006d))


### Bug Fixes

* [TE-1250](https://cortexdata.atlassian.net/browse/TE-1250) add roundOffThreshold param when using count as metric ([5c346275e](https://github.com/startreedata/thirdeye/commit/5c346275eb6b783a5d8581e23bc5998199e443b6))

## [2.6.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.5.0...thirdeye-ui-2.6.0) (2023-01-22)


### Features

* [TE-1203](https://cortexdata.atlassian.net/browse/TE-1203) added enumeration items to anomalies list table and metrics report table ([fda14f085](https://github.com/startreedata/thirdeye/commit/fda14f085b01ffea9b138c91a835ff3b3d58c424))
* [TE-1220](https://cortexdata.atlassian.net/browse/TE-1220) link to rca demo video in rca and anomaly details page ([13b288229](https://github.com/startreedata/thirdeye/commit/13b2882296d6ffc3d9e785e2d44748b871e0c4c5))
* [TE-1240](https://cortexdata.atlassian.net/browse/TE-1240) using total anomaly count to calculate alert accuracy ([80e387aa3](https://github.com/startreedata/thirdeye/commit/80e387aa34f19d5cf407e0e13a7aa173efdddec7))


### Bug Fixes

* [TE-1228](https://cortexdata.atlassian.net/browse/TE-1228) use existing value for sensitivity if it exists for the slider in new user flow ([29b045587](https://github.com/startreedata/thirdeye/commit/29b04558753d54c489255485e6ee3f3ff208cb21))

## [2.5.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.4.0...thirdeye-ui-2.5.0) (2023-01-18)


### Features

* [TE-1004](https://cortexdata.atlassian.net/browse/TE-1004) toggle showing/hiding ignored anomalies ([434d8abb0](https://github.com/startreedata/thirdeye/commit/434d8abb0bc8c4ebdd9a335c95462eb1691eddd6))
* [TE-1211](https://cortexdata.atlassian.net/browse/TE-1211) deleting timestamp keys from copied alert ([4a9c82546](https://github.com/startreedata/thirdeye/commit/4a9c82546f4348688694d0981588ee3b88e32c4d))
* [TE-1214](https://cortexdata.atlassian.net/browse/TE-1214) fixed chart legend toggling series for custom render ([2b93cf5ce](https://github.com/startreedata/thirdeye/commit/2b93cf5ce7848df10390b0a18bea64d7e6e5abb4))

## [2.4.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.3.0...thirdeye-ui-2.4.0) (2023-01-12)


### Features

* [TE-1182](https://cortexdata.atlassian.net/browse/TE-1182) removed `timeColumn` and `timeColumnFormat` from `templateProperties` ([d51dde3a4](https://github.com/startreedata/thirdeye/commit/d51dde3a45772ddedb4e1ff153dd0922e472129f))


### Bug Fixes

* [TE-1205](https://cortexdata.atlassian.net/browse/TE-1205) fix issue where a lot datapoints break `getMinMax` ([47077df18](https://github.com/startreedata/thirdeye/commit/47077df1856fd5a66553028b30f2c13dc25cf600))
* [TE-1208](https://cortexdata.atlassian.net/browse/TE-1208) remove 10 second wait and background check for anomalies generated ([db699828c](https://github.com/startreedata/thirdeye/commit/db699828cc6b076df341462187a5cf58e16d30bd))

## [2.3.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.2.0...thirdeye-ui-2.3.0) (2023-01-11)


### Features

* [TE-1155](https://cortexdata.atlassian.net/browse/TE-1155) update anomaly stats on homepage for date range change ([40fa84984](https://github.com/startreedata/thirdeye/commit/40fa8498470214faced25a7c2f83a90c8e2092c2))
* [TE-1178](https://cortexdata.atlassian.net/browse/TE-1178) integrate creating subscription group on guided create alert flow ([6b9dc5feb](https://github.com/startreedata/thirdeye/commit/6b9dc5feb760e7c5529d82a099bf7aeee81c599f))
* [TE-1179](https://cortexdata.atlassian.net/browse/TE-1179) alert accuracy only shown if anomalies are present ([35655bed2](https://github.com/startreedata/thirdeye/commit/35655bed2f8f30e68552bc855c205e53114b2ab3))

## [2.2.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.1.0...thirdeye-ui-2.2.0) (2023-01-09)


### Features

* [TE-1108](https://cortexdata.atlassian.net/browse/TE-1108) redirect to welcome flow if no alerts exist ([9c8b3909f](https://github.com/startreedata/thirdeye/commit/9c8b3909f321716c261d18973d616e69147a6cee))
* [TE-1149](https://cortexdata.atlassian.net/browse/TE-1149) add markdown support in alert create form helper tooltip ([1d13b6582](https://github.com/startreedata/thirdeye/commit/1d13b65822ebdf1602f0f4d0049b3caac72aac4a))
* [TE-1159](https://cortexdata.atlassian.net/browse/TE-1159) update sample alerts ([a2ab4896a](https://github.com/startreedata/thirdeye/commit/a2ab4896a7174d9800952408a2979544c29ce088))
* [TE-1163](https://cortexdata.atlassian.net/browse/TE-1163) browser console cleanup for ThirdEye UI ([1f5bac0a2](https://github.com/startreedata/thirdeye/commit/1f5bac0a284869703cd6965430e4aab44d61b282))
* [TE-1194](https://cortexdata.atlassian.net/browse/TE-1194) make users wait 10 seconds after creating an alert before redirect ([882e2cca3](https://github.com/startreedata/thirdeye/commit/882e2cca3cd26cf41a452d6223926b40a3e54699))
* [TE-1196](https://cortexdata.atlassian.net/browse/TE-1196) replace blank recent anomalies chart with message ([25c3da8d9](https://github.com/startreedata/thirdeye/commit/25c3da8d96b53a8d064fac58a0f273f484e95220))


### Bug Fixes

* [TE-1179](https://cortexdata.atlassian.net/browse/TE-1179) display no root cause found in top contributors table ([01be2a769](https://github.com/startreedata/thirdeye/commit/01be2a769221a6710c3805235359f599fb459ed7))
* [TE-1198](https://cortexdata.atlassian.net/browse/TE-1198) preview chart styling fix in alert creation simple mode ([1da512c96](https://github.com/startreedata/thirdeye/commit/1da512c96e825f337e1497fe2434da7836d771b0))

## [2.1.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-2.0.0...thirdeye-ui-2.1.0) (2023-01-03)


### Features

* [TE-1148](https://cortexdata.atlassian.net/browse/TE-1148) added a tool-tip for for alert form field description ([ff166df9e](https://github.com/startreedata/thirdeye/commit/ff166df9e85e1be7beb2d41cb2ebf0fbe5a47f3f))
* [TE-1156](https://cortexdata.atlassian.net/browse/TE-1156) allow users to navigate to all alerts after successful alert creation from welcome flow ([49488a4b0](https://github.com/startreedata/thirdeye/commit/49488a4b05c7de7e27b8341e5d1e6c66489146c6))
* [TE-1160](https://cortexdata.atlassian.net/browse/TE-1160) autofill metric, datasource, and dataset if they exist when using cohort recommender ([432e423f2](https://github.com/startreedata/thirdeye/commit/432e423f24eda830319ca82ac27f6c5e113d6ce8))


### Bug Fixes

* [TE-1152](https://cortexdata.atlassian.net/browse/TE-1152) incorporating the properties metadata into the validation for alert creation ([adff7ffcb](https://github.com/startreedata/thirdeye/commit/adff7ffcb10ac97beca92aba4580f77e38dd953b))
* [TE-1153](https://cortexdata.atlassian.net/browse/TE-1153) append random 3 character string to sample alert name when creating ([ce116c19c](https://github.com/startreedata/thirdeye/commit/ce116c19cf12cfa8a06f70102500154a20aa46ad))
* [TE-1154](https://cortexdata.atlassian.net/browse/TE-1154) ensure baselineOffset is a string ([fc3684ec6](https://github.com/startreedata/thirdeye/commit/fc3684ec62b40939ea0c143a9bb1eca59c71d41a))
* [TE-1157](https://cortexdata.atlassian.net/browse/TE-1157) fix bug where invalid json will cause white screen ([492f5b1b7](https://github.com/startreedata/thirdeye/commit/492f5b1b788cdd66091ff2168d12045fd652c5c5))
* [TE-1158](https://cortexdata.atlassian.net/browse/TE-1158) ensure valid start and end exist when fetching data ([2426ea609](https://github.com/startreedata/thirdeye/commit/2426ea609f1f897b31ec56e2644fb54fd181b773))
* ensure loading state is shown when loading alerts ([5e11a3c08](https://github.com/startreedata/thirdeye/commit/5e11a3c080378c84db382cff37600390177d5d1d))

## [2.0.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.75.0...thirdeye-ui-2.0.0) (2022-12-20)


### Features

* [TE-1150](https://cortexdata.atlassian.net/browse/TE-1150) turn alert name into href in alerts table ([ec073223b](https://github.com/startreedata/thirdeye/commit/ec073223b67a5c34fc6812a45b9d6a57e3a0be81))


### Major

* 2.0 releae for ui ([9dd087e3e](https://github.com/startreedata/thirdeye/commit/9dd087e3e48c7afa7648a4f5de482d70dc4f30a7))

## [1.75.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.74.0...thirdeye-ui-1.75.0) (2022-12-16)


### Features

* [TE-1118](https://cortexdata.atlassian.net/browse/TE-1118) enhancements to the welcome alert creation flow ([ee32548f1](https://github.com/startreedata/thirdeye/commit/ee32548f12507b8d357a55fa93ead84ad330c186))
* [TE-1119](https://cortexdata.atlassian.net/browse/TE-1119) first iteration of new alert creation flow integration with configuration page ([6abe93f59](https://github.com/startreedata/thirdeye/commit/6abe93f59ec03a9b5ee46b1aa28bb006c937ad6f))
* [TE-1121](https://cortexdata.atlassian.net/browse/TE-1121) wire up the sample alert buttons and hide them if missing dataset ([475782a65](https://github.com/startreedata/thirdeye/commit/475782a65950feeb95c2d11160f19209b9008f6c))
* [TE-1127](https://cortexdata.atlassian.net/browse/TE-1127) added alert accuracy fetching to alerts list table ([786960d5a](https://github.com/startreedata/thirdeye/commit/786960d5a022bab7b08d28aea52b2c25f43925f2))


### Bug Fixes

* [TE-1119](https://cortexdata.atlassian.net/browse/TE-1119) wrap simple and advance mode edit pages in grid ([bbdbded11](https://github.com/startreedata/thirdeye/commit/bbdbded1193fe5cd0d007298720853b033f5cadd))
* [TE-1138](https://cortexdata.atlassian.net/browse/TE-1138) more enhancements to the alert creation ui ([9c697a8c3](https://github.com/startreedata/thirdeye/commit/9c697a8c370663778c6e5d3b88b91f6014b314ae))
* [TE-1147](https://cortexdata.atlassian.net/browse/TE-1147) fix bug where UI should send non quoted strings ([f1c3443e1](https://github.com/startreedata/thirdeye/commit/f1c3443e149d8d6bd11d0a52f308aaeb298b6501))
* ensure reactnode is returned in LoadingErrorStateSwitch ([4cb355409](https://github.com/startreedata/thirdeye/commit/4cb3554095129cb592e09d38c2f4ca2e01c69c01))

## [1.74.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.73.0...thirdeye-ui-1.74.0) (2022-12-12)


### Features

* [TE-1082](https://cortexdata.atlassian.net/browse/TE-1082) added recipes and alert examples links to document section ([71f4e7536](https://github.com/startreedata/thirdeye/commit/71f4e7536b2aaa656e94d9eaf0946f1dee728bd2))
* [TE-1105](https://cortexdata.atlassian.net/browse/TE-1105) recent anomalies home page ([f57ecd526](https://github.com/startreedata/thirdeye/commit/f57ecd5263aa740609f3d9f2c2c8cd9e2a02155e))
* [TE-1120](https://cortexdata.atlassian.net/browse/TE-1120) added intro video to welcome screen ([8d9508212](https://github.com/startreedata/thirdeye/commit/8d9508212ab9d555326f68c6658a0d8b625204b4))
* [TE-1128](https://cortexdata.atlassian.net/browse/TE-1128) fix overflow for long text in enumeration table ([9d78d662e](https://github.com/startreedata/thirdeye/commit/9d78d662ea0623470fe212ccfaecb4a341f1d7c6))
* [TE-1129](https://cortexdata.atlassian.net/browse/TE-1129) added property description to alert template builder ([d0713a847](https://github.com/startreedata/thirdeye/commit/d0713a847e298499d34ad7a73c1f38a50f59b3bd))


### Bug Fixes

* [TE-1112](https://cortexdata.atlassian.net/browse/TE-1112) show empty message when alert templates for a section is missing ([60f68a134](https://github.com/startreedata/thirdeye/commit/60f68a134e139be1bf3524eaf5fbc61624f9cb7a))

## [1.73.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.72.1...thirdeye-ui-1.73.0) (2022-12-05)


### Features

* [TE-1068](https://cortexdata.atlassian.net/browse/TE-1068) added sidebar link to swagger ([05855341b](https://github.com/startreedata/thirdeye/commit/05855341bf3b02f769c86137a0b43aa982582866))
* [TE-1072](https://cortexdata.atlassian.net/browse/TE-1072) data config flow ([e6238b8fc](https://github.com/startreedata/thirdeye/commit/e6238b8fc0b256855edb0dd3e7b1a2d342b512fd))
* [TE-1092](https://cortexdata.atlassian.net/browse/TE-1092) parsing `properties` key in alert template for default properties ([84b01bdf2](https://github.com/startreedata/thirdeye/commit/84b01bdf2f684d9419a8432a7e930c05aa81d647))
* [TE-1106](https://cortexdata.atlassian.net/browse/TE-1106) integrate dimension exploration flow into the welcome create alert flow ([a424d67af](https://github.com/startreedata/thirdeye/commit/a424d67afc2b04677d15ee8f9d88555563f426e1))

### [1.72.1](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.72.0...thirdeye-ui-1.72.1) (2022-11-28)


### Bug Fixes

* [TE-1055](https://cortexdata.atlassian.net/browse/TE-1055) assigned fixed height to boxes on home page ([8e78dedea](https://github.com/startreedata/thirdeye/commit/8e78dedea3808574f14a2fcbecfe70d22d0feeeb))

## [1.72.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.71.0...thirdeye-ui-1.72.0) (2022-11-19)


### Features

* [TE-1052](https://cortexdata.atlassian.net/browse/TE-1052) cohort recommender enhancements ([56a94d23b](https://github.com/startreedata/thirdeye/commit/56a94d23b1640a2f3f78bb3ee37a281d589225fe))


### Bug Fixes

* [TE-1061](https://cortexdata.atlassian.net/browse/TE-1061) showing a message for 0 anomalies on home page ([d0e4e8264](https://github.com/startreedata/thirdeye/commit/d0e4e8264b4cd77bd0daa90956cb408e37df5674))

## [1.71.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.70.0...thirdeye-ui-1.71.0) (2022-11-16)


### Features

* [TE-1002](https://cortexdata.atlassian.net/browse/TE-1002) added date filter in admin page and set the default filter to last 7 days ([948e90fc5](https://github.com/startreedata/thirdeye/commit/948e90fc55c7a51d2385c22273c25c2cec298df2))


### Bug Fixes

* [TE-1059](https://cortexdata.atlassian.net/browse/TE-1059) renamed labels and replaced a fixed label with a generic label ([8158ca92b](https://github.com/startreedata/thirdeye/commit/8158ca92b9fb4fa46ab23407a86a5758119930d3))

## [1.70.0](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.69.6...thirdeye-ui-1.70.0) (2022-11-14)


### Features

* [TE-1056](https://cortexdata.atlassian.net/browse/TE-1056) replaced the create alert button with load default alerts button on empty alerts ([7bff32af7](https://github.com/startreedata/thirdeye/commit/7bff32af78df41035903ff005214f280a636d603))
* [TE-1058](https://cortexdata.atlassian.net/browse/TE-1058) added message for empty dataset for anomaly graph on home page ([70f053b74](https://github.com/startreedata/thirdeye/commit/70f053b7467d22baf35e32d2cceedbe142fedc86))


### Bug Fixes

* [TE-1051](https://cortexdata.atlassian.net/browse/TE-1051) renamed cohort-detector route to be consistent with page title ([801ab94ac](https://github.com/startreedata/thirdeye/commit/801ab94acb4f2b6a3fa351faf68e1568ac6ab65c))

### [1.69.6](https://github.com/startreedata/thirdeye/compare/thirdeye-ui-1.69.5...thirdeye-ui-1.69.6) (2022-11-08)


### Bug Fixes

* [TE-1045](https://cortexdata.atlassian.net/browse/TE-1045) ensure alerts stay within screen ([64ecd7d80](https://github.com/startreedata/thirdeye/commit/64ecd7d802e51d1e94c90e803bd3cb3b7e7e9fc7))

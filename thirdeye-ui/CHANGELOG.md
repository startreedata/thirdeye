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

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

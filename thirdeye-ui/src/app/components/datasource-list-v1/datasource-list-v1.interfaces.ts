// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";

export interface DatasourceListV1Props {
    datasources: UiDatasource[] | null;
    onDelete?: (uiDatasource: UiDatasource) => void;
}

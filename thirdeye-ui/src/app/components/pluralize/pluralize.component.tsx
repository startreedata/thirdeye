// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import React, { FunctionComponent } from "react";
import { PluralizeProps } from "./pluralize.interfaces";

export const Pluralize: FunctionComponent<PluralizeProps> = ({
    count,
    singular,
    plural,
}) => {
    if (count === 0) {
        return (
            <span>
                {count} {plural}
            </span>
        );
    }

    return count > 1 ? (
        <span>
            {count} {plural}
        </span>
    ) : (
        <span>
            {count} {singular}
        </span>
    );
};

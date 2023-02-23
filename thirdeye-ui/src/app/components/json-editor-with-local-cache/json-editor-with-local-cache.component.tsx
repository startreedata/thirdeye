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
import React, { ReactElement, useState } from "react";
import { JSONEditorV1 } from "../../platform/components";
import { JSONEditorWithLocalCacheProps } from "./json-editor-with-local-cache.interfaces";

export function JSONEditorWithLocalCache<T extends Record<string, unknown>>({
    initialValue,
    onChange,
    ...otherProps
}: JSONEditorWithLocalCacheProps<T>): ReactElement {
    const [localCopy, setLocalCopy] = useState<T>(
        initialValue || otherProps.value || ({} as T)
    );

    const handleChange = (updates: string): void => {
        try {
            const parsedString = JSON.parse(updates);
            setLocalCopy(() => parsedString);
            onChange && onChange(updates);
        } catch {
            // do nothing if invalid JSON string
        }
    };

    return (
        <JSONEditorV1
            {...otherProps}
            value={JSON.stringify(localCopy)}
            onChange={handleChange}
        />
    );
}

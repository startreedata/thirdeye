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

import { Checkbox, TableCell, TableRow } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { getAssociationId } from "../../alerts-dimensions.utils";
import { EnumerationItemRowProps } from "./enumeration-item-row.interfaces";

export const EnumerationItemRow: FunctionComponent<EnumerationItemRowProps> = ({
    alert,
    enumerationItem,
    associations,
    onAssociationChange,
    isDimensionSelectOn,
    overrideSelected,
}) => {
    const handleEnumerationItemCheckChange = (
        enumerationItemId: number,
        checked: boolean
    ): void => {
        onAssociationChange((currentAssociations) => {
            let copied = [...currentAssociations];

            if (checked) {
                copied.push({
                    alertId: alert.id,
                    enumerationId: enumerationItemId,
                    id: getAssociationId({
                        alertId: alert.id,
                        enumerationId: enumerationItemId,
                    }),
                });
            } else {
                copied = copied.filter(
                    (association) =>
                        association.enumerationId !== enumerationItemId
                );
            }

            return copied;
        });
    };

    // overrideSelected overrides the check
    const isChecked =
        overrideSelected ||
        associations.find(
            (candidate) =>
                candidate.alertId === alert.id &&
                candidate.enumerationId === enumerationItem.id
        ) !== undefined;

    return (
        <TableRow key={enumerationItem.id}>
            <TableCell align="left" padding="checkbox">
                <Checkbox
                    checked={isChecked}
                    color={isDimensionSelectOn ? "primary" : undefined}
                    disabled={!isDimensionSelectOn}
                    onChange={(_, checked) =>
                        handleEnumerationItemCheckChange(
                            enumerationItem.id,
                            checked
                        )
                    }
                />
            </TableCell>
            <TableCell>{enumerationItem.name}</TableCell>
        </TableRow>
    );
};

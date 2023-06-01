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
import {
    Box,
    Button,
    Dialog,
    DialogContent,
    DialogTitle,
    Grid,
} from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { ModalProps } from "./modal.interfaces";

export const Modal: FunctionComponent<ModalProps> = ({
    trigger,
    submitButtonLabel,
    cancelButtonLabel,
    onSubmit,
    onCancel,
    title,
    customTitle,
    children,
    maxWidth,
    footerActions,
    onOpen,
}) => {
    const { t } = useTranslation();
    const [isOpen, setIsOpen] = useState(false);

    const handleCancelClick = (): void => {
        setIsOpen(false);
        onCancel && onCancel();
    };

    const handleSubmit = (): void => {
        if (!onSubmit) {
            return;
        }

        // Response of false means keep modal open
        onSubmit() !== false && setIsOpen(false);
    };

    return (
        <>
            {trigger(() => {
                onOpen && onOpen();
                setIsOpen(true);
            })}

            <Dialog
                maxWidth={maxWidth}
                open={isOpen}
                onClose={() => setIsOpen(false)}
            >
                {title && <DialogTitle>{title}</DialogTitle>}
                {customTitle}

                <DialogContent dividers>{children}</DialogContent>

                <Box pb={2} pl={2} pr={2} pt={2}>
                    <Grid container justifyContent="space-between">
                        <Grid item>{footerActions}</Grid>
                        <Grid item>
                            <Grid container spacing={2}>
                                <Grid item>
                                    <Button
                                        color="secondary"
                                        onClick={handleCancelClick}
                                    >
                                        {cancelButtonLabel || t("label.cancel")}
                                    </Button>
                                </Grid>
                                <Grid item>
                                    {onSubmit && (
                                        <Button
                                            color="primary"
                                            type="submit"
                                            onClick={handleSubmit}
                                        >
                                            {submitButtonLabel ||
                                                t("label.submit")}
                                        </Button>
                                    )}
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Box>
            </Dialog>
        </>
    );
};

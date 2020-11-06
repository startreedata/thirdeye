import { Box, Typography } from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import React, { FunctionComponent, ReactNode } from "react";
import { Button } from "../button/button.component";
import CommonCodeMirror from "../editor/code-mirror.component";

type Props = {
    name: string;
    extraFields: ReactNode;
    showPreviewButton?: boolean;
    config: string;
    onConfigChange: (newValue: string) => void;
    onResetConfig: () => void;
};

export const ConfigurationStep: FunctionComponent<Props> = ({
    name,
    extraFields,
    showPreviewButton,
    config,
    onConfigChange,
    onResetConfig,
}: Props) => {
    return (
        <Box display="flex" flexDirection="column">
            <Typography variant="h4">{name}</Typography>
            <Box display="flex" justifyContent="space-between">
                {extraFields}
                <Button
                    color="primary"
                    startIcon={<RefreshIcon />}
                    variant="text"
                    onClick={onResetConfig}
                >
                    Reset Configuration
                </Button>
            </Box>
            <CommonCodeMirror
                options={{
                    mode: "text/x-yaml",
                    indentWithTabs: true,
                    smartIndent: true,
                    lineNumbers: true,
                    lineWrapping: true,
                    extraKeys: { "'@'": "autocomplete" },
                }}
                value={config}
                onChange={onConfigChange}
            />
            {showPreviewButton && (
                <Box>
                    <Button disabled color="primary" variant="text">
                        Preview Alert
                    </Button>
                </Box>
            )}
        </Box>
    );
};

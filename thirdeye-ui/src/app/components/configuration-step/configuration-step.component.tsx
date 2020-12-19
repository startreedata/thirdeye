import { Box, Grid, Typography } from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import { isUndefined } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AlertEvaluationTimeSeriesCard } from "../alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { Button } from "../button/button.component";
import CommonCodeMirror from "../editor/code-mirror.component";
import { ConfigStepsProps } from "./configuration-step.interfaces";

export const ConfigurationStep: FunctionComponent<ConfigStepsProps> = ({
    name,
    extraFields,
    showPreviewButton,
    config,
    previewData,
    onConfigChange,
    onResetConfig,
    onPreviewAlert,
    editorProps,
}: ConfigStepsProps) => {
    const { t } = useTranslation();
    const handlePreviewAlert = (): void => {
        if (typeof onPreviewAlert === "function") {
            onPreviewAlert();
        }
    };

    return (
        <Grid container>
            <Grid item xs={12}>
                <Typography variant="h4">{name}</Typography>
            </Grid>
            <Grid item xs={12}>
                <Box
                    alignItems="center"
                    display="flex"
                    justifyContent="space-between"
                >
                    {extraFields}
                    <Button
                        color="primary"
                        startIcon={<RefreshIcon />}
                        variant="text"
                        onClick={onResetConfig}
                    >
                        {t("label.reset-configuration")}
                    </Button>
                </Box>
            </Grid>
            <Grid item xs={12}>
                <CommonCodeMirror
                    options={{
                        mode: "text/x-yaml",
                        indentWithTabs: true,
                        smartIndent: true,
                        lineNumbers: true,
                        lineWrapping: true,
                        extraKeys: { "'@'": "autocomplete" },
                        ...(editorProps?.options || {}),
                    }}
                    value={config}
                    onChange={onConfigChange}
                />
            </Grid>
            {showPreviewButton && (
                <Grid container item direction="row" justify="space-between">
                    <Grid item>
                        <Box>
                            <Button
                                color="primary"
                                variant="text"
                                onClick={handlePreviewAlert}
                            >
                                {t("label.preview-alert")}
                            </Button>
                        </Box>
                    </Grid>
                </Grid>
            )}
            {!isUndefined(previewData) ? (
                <Grid item xs={12}>
                    <AlertEvaluationTimeSeriesCard
                        alertEvaluation={previewData}
                    />
                </Grid>
            ) : null}
        </Grid>
    );
};

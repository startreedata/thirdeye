import { FormHelperText } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetDatasourceStatus } from "../../../rest/datasources/datasources.actions";
import { OK_STATUS } from "../../../rest/datasources/datasources.rest";
import { DatasourceVerificationProps } from "./datasource-verification.interfaces";
import { useDatasourceVerificationStyles } from "./datasource-verification.styles";

export const DatasourceVerification: FunctionComponent<DatasourceVerificationProps> =
    ({ datasourceName }) => {
        const classes = useDatasourceVerificationStyles();
        const {
            getDatasourceStatus,
            healthStatus,
            errorMessages,
            status: requestStatus,
        } = useGetDatasourceStatus();
        const { t } = useTranslation();

        useEffect(() => {
            getDatasourceStatus(datasourceName);
        }, [datasourceName]);

        if (requestStatus === ActionStatus.Working) {
            return (
                <FormHelperText>
                    {t("message.checking-datasource-health")}
                </FormHelperText>
            );
        }

        // Server Error
        if (requestStatus === ActionStatus.Error) {
            return (
                <>
                    <FormHelperText error>
                        {t(
                            "message.error-encountered-while-fetching-datasource-status"
                        )}
                    </FormHelperText>
                    {errorMessages.map((msg, idx) => (
                        <FormHelperText error key={`${idx}`}>
                            {msg}
                        </FormHelperText>
                    ))}
                </>
            );
        }

        if (requestStatus === ActionStatus.Done && healthStatus) {
            if (healthStatus.code === OK_STATUS) {
                return (
                    <FormHelperText className={classes.successText}>
                        {t("message.datasource-is-healthy")}
                    </FormHelperText>
                );
            } else if (healthStatus.list) {
                return (
                    <>
                        {healthStatus.list.map((errorStatus, idx) => (
                            <FormHelperText error key={`${idx}`}>
                                {errorStatus.msg}
                            </FormHelperText>
                        ))}
                    </>
                );
            }
        }

        return null;
    };

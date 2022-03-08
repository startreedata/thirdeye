import { Button, Grid, Link } from "@material-ui/core";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridV1,
    JSONEditorV1,
    PageContentsCardV1,
} from "../../platform/components";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import {
    getAlertTemplatesUpdatePath,
    getAlertTemplatesViewPath,
} from "../../utils/routes/routes.util";
import { AlertTemplateListV1Props } from "./alert-template-list-v1.interfaces";

export const AlertTemplateListV1: FunctionComponent<
    AlertTemplateListV1Props
> = (props) => {
    const [selectedAlertTemplate, setSelectedAlertTemplate] =
        useState<DataGridSelectionModelV1<AlertTemplate>>();
    const [alertTemplatesData, setAlertTemplatesData] = useState<
        AlertTemplate[] | null
    >(null);
    const navigate = useNavigate();

    const { t } = useTranslation();

    const generateDataWithChildren = (
        data: AlertTemplate[]
    ): AlertTemplate[] => {
        return data?.map((alertTemplate, index) => ({
            ...alertTemplate,
            children: [
                {
                    id: index,
                    expandPanelContents: (
                        <JSONEditorV1<AlertTemplate>
                            readOnly
                            value={alertTemplate}
                        />
                    ),
                },
            ],
        }));
    };

    useEffect(() => {
        if (!props.alertTemplates) {
            return;
        }

        setAlertTemplatesData(generateDataWithChildren(props.alertTemplates));
    }, [props.alertTemplates]);

    const renderLink = (
        cellValue: Record<string, unknown>,
        data: AlertTemplate
    ): ReactElement => {
        if (cellValue) {
            return (
                <Link href={getAlertTemplatesViewPath(data.id)}>
                    {cellValue}
                </Link>
            );
        } else {
            return <span />;
        }
    };

    const isActionButtonDisable = !(
        selectedAlertTemplate && selectedAlertTemplate.rowKeyValues.length === 1
    );

    const handleAlertTemplateDelete = (): void => {
        if (isActionButtonDisable) {
            return;
        }
        const selectedAlertTemplate: AlertTemplate | undefined =
            props.alertTemplates?.find(
                (alertTemplate) =>
                    alertTemplate.id === selectedAlertTemplate?.id
            );

        selectedAlertTemplate &&
            props.onDelete &&
            props.onDelete(selectedAlertTemplate);
    };

    const handleAlertEdit = (): void => {
        if (!selectedAlertTemplate) {
            return;
        }
        const selectedAlertId = selectedAlertTemplate.rowKeyValues[0] as number;

        navigate(getAlertTemplatesUpdatePath(selectedAlertId));
    };

    const alertGroupColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 0,
            flex: 1.5,
            sortable: true,
            customCellRenderer: renderLink,
        },
        {
            key: "description",
            dataKey: "description",
            header: t("label.description"),
            minWidth: 0,
            flex: 1,
        },
    ];

    return (
        <Grid item xs={12}>
            <PageContentsCardV1 disablePadding fullHeight>
                <DataGridV1<AlertTemplate>
                    hideBorder
                    columns={alertGroupColumns}
                    data={alertTemplatesData as AlertTemplate[]}
                    expandColumnKey="name"
                    rowKey="id"
                    scroll={DataGridScrollV1.Body}
                    searchPlaceholder={t("label.search-entity", {
                        entity: t("label.alert-templates"),
                    })}
                    toolbarComponent={
                        <Grid container alignItems="center" spacing={2}>
                            {/* Edit */}
                            <Grid item>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleAlertEdit}
                                >
                                    {t("label.edit")}
                                </Button>
                            </Grid>

                            {/* Delete */}
                            <Grid>
                                <Button
                                    disabled={isActionButtonDisable}
                                    variant="contained"
                                    onClick={handleAlertTemplateDelete}
                                >
                                    {t("label.delete")}
                                </Button>
                            </Grid>
                        </Grid>
                    }
                    onSelectionChange={setSelectedAlertTemplate}
                />
            </PageContentsCardV1>
        </Grid>
    );
};

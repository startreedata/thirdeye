import { Button, Paper } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { concatKeyValueWithEqual } from "../../../../utils/params/params.util";
import { FiltersSetTableProps } from "./filters-set-table.interfaces";

export const FiltersSetTable: FunctionComponent<FiltersSetTableProps> = ({
    timeSeriesFiltersSet,
    onRemoveBtnClick,
}) => {
    const { t } = useTranslation();

    return (
        <TableContainer component={Paper}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>
                            <strong>{t("label.filter-sets")}</strong>
                        </TableCell>
                        <TableCell />
                    </TableRow>
                </TableHead>
                <TableBody>
                    {timeSeriesFiltersSet.map((filterOptions, idx) => {
                        const merged = filterOptions.map(
                            concatKeyValueWithEqual
                        );

                        return (
                            <TableRow key={merged.join()}>
                                <TableCell component="th" scope="row">
                                    {merged.join(" & ")}
                                </TableCell>
                                <TableCell align="right">
                                    <Button
                                        color="primary"
                                        variant="outlined"
                                        onClick={() => onRemoveBtnClick(idx)}
                                    >
                                        {t("label.remove")}
                                    </Button>
                                </TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

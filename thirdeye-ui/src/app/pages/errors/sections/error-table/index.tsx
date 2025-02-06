import React, { useMemo } from "react"
import DataTable from "../../../../platform/components/table/table.component";
import { getErrorObj } from "../../util";
import { Link } from "@material-ui/core";
import { Link as RouterLink } from "react-router-dom";

export const ErrorTable = () => {
  const renderAlertLink = (data: TableRow): ReactElement => {
    const id = Number(data.id);

    return (
        <Link component={RouterLink} to={`/errors/${id}/view`}>
            {data.type}
        </Link>
    );
};
  const columns: TableColumns[] = [
    {
        title: 'type',
        datakey: "type",
        customRender: renderAlertLink,
    },
    {
        title: 'message',
        datakey: "message",
    },
    {
        title: 'user',
        datakey: 'user'
    },
    {
        title: 'time',
        datakey: "time",
    },
  ];

  const errorData = useMemo(() => {
    const data: { id: number; user: string; type: string; message: string; time: string}[] = [];
      for (let i=0;i< 20;i++){
        data.push(getErrorObj(i))
      }
      return data;
    }, []);

    return (
    <>
      <DataTable
        columns={columns}
        data={errorData}
        emptyStateView={'Yaay! No errors. Go break it for me to stay in business'}
      />
    </>
  )
}
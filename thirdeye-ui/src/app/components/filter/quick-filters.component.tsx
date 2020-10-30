import { Box, Button, ButtonGroup, Grid, Link, makeStyles, Theme, Typography } from '@material-ui/core';
import React, { ReactElement } from 'react';


type Props = {
    filters: {
        name: string,
        value: string
    }[],
    onFilter: (value: string) => void
}

const useStyles = makeStyles((theme: Theme) => {
    return {
        root: {
            marginTop: 1
        },
        box: {
            alignItems: "center",
            display: "flex",
            justifyContent: "space-between",
            padding: theme.spacing(0, 2),
        },
        buttonGroup: {
            borderTop: '1px solid #BDBDBD', 
            borderBottom: '1px solid #BDBDBD', 
            borderRadius: 0
        },
        button: {
            justifyContent: 'left', 
            padding: '12px 16px' 
        }
    }
})


const QuickFilters = ({filters}: Props): ReactElement => {
    const classes = useStyles();

    return <Grid container className={classes.root} spacing={2}>
        <Grid item xs={12}>
            <Box className={classes.box} >
                <Typography variant="subtitle1">Quick Filters</Typography>
                <Typography variant="subtitle2"><Link href={'#'} underline={'none'} >Clear Filters</Link></Typography>
            </Box>
        </Grid>
        <Grid container >
            <ButtonGroup fullWidth className={classes.buttonGroup} orientation="vertical" variant="text" >
                {
                    filters.map(
                        (f: {name: string, value: string}): JSX.Element => {
                            return <Button className={classes.button} color="primary" key={f.name}>{f.name}</Button>
                        })
                }
            </ButtonGroup>
        </Grid>
    </Grid>
}

export default QuickFilters;
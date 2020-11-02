import { FormControlLabel, Typography } from "@material-ui/core";
import { CheckBox } from "@material-ui/icons";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import React, { ReactElement } from "react";
import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
} from "../styles/accordian.styles";

const DisplayFilter = (): ReactElement => {
    return (
        <Accordion expanded={true}>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="subtitle1">Display</Typography>
            </AccordionSummary>
            <AccordionDetails>
                <FormControlLabel
                    control={<CheckBox color="primary" />}
                    label="Active"
                />
                <br />
                <FormControlLabel
                    control={<CheckBox color="primary" />}
                    label="Inactive"
                />
            </AccordionDetails>
        </Accordion>
    );
};

export default DisplayFilter;

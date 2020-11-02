import { withStyles } from "@material-ui/core";
import MuiAccordion from "@material-ui/core/Accordion";
import MuiAccordionDetails from "@material-ui/core/AccordionDetails";
import MuiAccordionSummary from "@material-ui/core/AccordionSummary";

const Accordion = withStyles({
    root: {
        borderBottom: "1px solid  #BDBDBD",
        boxShadow: "none",
        "&:before": {
            display: "none",
        },
        "&$expanded": {
            margin: 0,
        },
    },
    expanded: {
        margin: 0,
        paddingBottom: "16px",
    },
})(MuiAccordion);

const AccordionSummary = withStyles({
    root: {
        marginBottom: -1,
        minHeight: 48,
        "&$expanded": {
            minHeight: 48,
        },
    },
    content: {
        "&$expanded": {
            margin: "9px 0",
        },
    },
    expanded: {},
})(MuiAccordionSummary);

const AccordionDetails = withStyles((theme) => {
    return {
        root: {
            padding: theme.spacing(0, 2),
        },
    };
})(MuiAccordionDetails);

export { Accordion, AccordionSummary, AccordionDetails };

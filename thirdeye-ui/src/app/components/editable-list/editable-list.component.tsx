import {
    Button,
    Card,
    CardContent,
    Grid,
    IconButton,
    InputAdornment,
    List,
    ListItem,
    ListItemSecondaryAction,
    ListItemText,
    TextField,
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import SubdirectoryArrowLeftIcon from "@material-ui/icons/SubdirectoryArrowLeft";
import React, {
    FunctionComponent,
    KeyboardEvent,
    useEffect,
    useRef,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../utils/material-ui/common.styles";
import { EditableListProps } from "./editable-list.interfaces";
import { useEditableListStyles } from "./editable-list.styles";

export const EditableList: FunctionComponent<EditableListProps> = (
    props: EditableListProps
) => {
    const editableListClasses = useEditableListStyles();
    const commonClasses = useCommonStyles();
    const [list, setList] = useState<string[]>([]);
    const [helperText, setHelperText] = useState("");
    const inputRef = useRef<HTMLInputElement>(null);
    const { t } = useTranslation();

    useEffect(() => {
        // Input changed, populate list
        setList(props.list || []);
    }, [props.list]);

    const handleInputKeyDown = (
        event: KeyboardEvent<HTMLInputElement>
    ): void => {
        if (event.key === "Enter") {
            handleAdd();
        }
    };

    const handleAdd = (): void => {
        if (!inputRef || !inputRef.current || !inputRef.current.value) {
            return;
        }

        const input = inputRef.current.value;
        let validationResult;
        if (
            (validationResult = props.validateFn && props.validateFn(input)) &&
            !validationResult.valid
        ) {
            // Validation failed
            setHelperText(
                validationResult.message || t("message.validation-failed")
            );

            return;
        }

        const newList = [input, ...list];
        setList(newList);
        reset();

        // Notify
        props.onChange && props.onChange(newList);
    };

    const reset = (): void => {
        if (!inputRef || !inputRef.current || !inputRef.current.value) {
            return;
        }

        // Clear input
        inputRef.current.value = "";
        setHelperText("");

        // Set focus
        inputRef.current.focus();
    };

    const handleRemove = (index: number) => (): void => {
        const newList = [...list];
        newList.splice(index, 1);
        setList(newList);

        // Notify
        props.onChange && props.onChange(newList);
    };

    return (
        <Grid container>
            {/* Input */}
            <Grid item sm={10} xs={9}>
                <TextField
                    fullWidth
                    InputProps={{
                        endAdornment: (
                            // Add button
                            <InputAdornment position="end">
                                <IconButton onClick={handleAdd}>
                                    <SubdirectoryArrowLeftIcon />
                                </IconButton>
                            </InputAdornment>
                        ),
                    }}
                    error={Boolean(helperText)}
                    helperText={helperText}
                    inputRef={inputRef}
                    label={props.inputLabel || t("label.add")}
                    variant="outlined"
                    onKeyDown={handleInputKeyDown}
                />
            </Grid>

            {/* Add button */}
            <Grid item sm={2} xs={3}>
                <Button
                    fullWidth
                    className={editableListClasses.addButton}
                    color="primary"
                    variant="outlined"
                    onClick={handleAdd}
                >
                    {props.addButtonLabel || t("label.add")}
                </Button>
            </Grid>

            {/* List */}
            <Grid item xs={12}>
                <Card variant="outlined">
                    <CardContent className={editableListClasses.list}>
                        <List dense>
                            {list &&
                                list.map((listItem, index) => (
                                    <ListItem button key={index}>
                                        <ListItemText
                                            primary={listItem}
                                            primaryTypographyProps={{
                                                variant: "body1",
                                                className:
                                                    commonClasses.ellipsis,
                                            }}
                                        />

                                        {/* Remove button */}
                                        <ListItemSecondaryAction>
                                            <IconButton
                                                onClick={handleRemove(index)}
                                            >
                                                <CloseIcon />
                                            </IconButton>
                                        </ListItemSecondaryAction>
                                    </ListItem>
                                ))}
                        </List>
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};

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
import { Close, SubdirectoryArrowLeft } from "@material-ui/icons";
import React, {
    createRef,
    FunctionComponent,
    KeyboardEvent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { EditableListProps } from "./editable-list.interfaces";
import { useEditableListStyles } from "./editable-list.styles";

export const EditableList: FunctionComponent<EditableListProps> = (
    props: EditableListProps
) => {
    const editableListClasses = useEditableListStyles();
    const [list, setList] = useState<string[]>([]);
    const [helperText, setHelperText] = useState("");
    const inputRef = createRef<HTMLInputElement>();
    const { t } = useTranslation();

    useEffect(() => {
        // Input changed, populate list
        setList(props.list || []);
    }, [props.list]);

    const onInputKeyPress = (event: KeyboardEvent<HTMLInputElement>): void => {
        if (event.key === "Enter") {
            onAddListItem();
        }
    };

    const onAddListItem = (): void => {
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

    const onRemoveListItem = (index: number) => (): void => {
        const newList = [...list];
        newList.splice(index, 1);
        setList(newList);

        // Notify
        props.onChange && props.onChange(list);
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

    return (
        <Grid container>
            {/* Input */}
            <Grid item sm={9}>
                <TextField
                    fullWidth
                    InputProps={{
                        endAdornment: (
                            // Add button
                            <InputAdornment position="end">
                                <IconButton onClick={onAddListItem}>
                                    <SubdirectoryArrowLeft />
                                </IconButton>
                            </InputAdornment>
                        ),
                    }}
                    error={Boolean(helperText)}
                    helperText={helperText}
                    inputRef={inputRef}
                    label={props.inputLabel || t("label.add")}
                    variant="outlined"
                    onKeyPress={onInputKeyPress}
                />
            </Grid>

            {/* Add button */}
            <Grid item sm={3}>
                <Button
                    fullWidth
                    className={editableListClasses.addButton}
                    color="primary"
                    variant="outlined"
                    onClick={onAddListItem}
                >
                    {props.addButtonLabel || t("label.add")}
                </Button>
            </Grid>

            {/* List */}
            <Grid item sm={12}>
                <Card variant="outlined">
                    <CardContent className={editableListClasses.listContainer}>
                        <List dense>
                            {list &&
                                list.map((listItem, index) => (
                                    <ListItem button key={index}>
                                        <ListItemText
                                            primary={listItem}
                                            primaryTypographyProps={{
                                                variant: "body1",
                                                className:
                                                    editableListClasses.listItem,
                                            }}
                                        />

                                        {/* Remove button */}
                                        <ListItemSecondaryAction>
                                            <IconButton
                                                onClick={onRemoveListItem(
                                                    index
                                                )}
                                            >
                                                <Close />
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

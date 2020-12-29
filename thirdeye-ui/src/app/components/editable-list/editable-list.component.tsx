import {
    Button,
    Card,
    CardContent,
    Grid,
    IconButton,
    List,
    ListItem,
    ListItemSecondaryAction,
    ListItemText,
    TextField,
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import SubdirectoryArrowLeftIcon from "@material-ui/icons/SubdirectoryArrowLeft";
import React, { useState } from "react";
import { EditableListProps, ValidationError } from "./editable-list.interfaces";
import { useEditableListStyles } from "./editable-list.styles";

const validState = {
    message: "",
    valid: true,
} as ValidationError;

export function EditableList({
    list,
    inputLabel,
    buttonLabel,
    onChange,
    validate,
}: EditableListProps): JSX.Element {
    const [value, setValue] = useState<string>("");
    const [isValid, setIsValid] = useState<ValidationError>(validState);
    const listAddEditClasses = useEditableListStyles();

    const onAdd = (): void => {
        const valid = validate && validate(value);
        if (!valid?.valid) {
            setIsValid(valid as ValidationError);

            return;
        }

        onChange([...list, value]);
        setValue("");
        setIsValid(validState);
    };

    const onRemove = (idx: number) => (): void => {
        onChange(list.filter((_t, index) => index !== idx));
    };

    return (
        <Grid container>
            <Grid item md={9}>
                <TextField
                    fullWidth
                    error={!isValid?.valid}
                    helperText={isValid?.message}
                    label={inputLabel}
                    value={value}
                    variant="outlined"
                    onChange={(
                        event: React.ChangeEvent<HTMLInputElement>
                    ): void => {
                        setIsValid(validState);
                        setValue(event.target.value);
                    }}
                    onKeyPress={({
                        key,
                    }: React.KeyboardEvent<HTMLInputElement>): void => {
                        if (key === "Enter") {
                            onAdd();
                        }
                    }}
                />
            </Grid>
            <Grid item md={3}>
                <Button
                    fullWidth
                    color="primary"
                    endIcon={<SubdirectoryArrowLeftIcon />}
                    size="large"
                    style={{
                        padding: "15px 0px",
                    }}
                    variant="outlined"
                    onClick={onAdd}
                >
                    {buttonLabel}
                </Button>
            </Grid>
            <Grid item md={12}>
                <Card variant="outlined">
                    <CardContent className={listAddEditClasses.listContainer}>
                        <List dense>
                            {list.map((item, idx) => (
                                <ListItem
                                    button
                                    key={idx}
                                    onClick={(): void => {
                                        onRemove(idx);
                                    }}
                                >
                                    <ListItemText primary={item} />
                                    <ListItemSecondaryAction>
                                        <IconButton onClick={onRemove(idx)}>
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
}

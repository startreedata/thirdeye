import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import {
    useGetAlertTemplate,
    useGetAlertTemplates,
} from "./alert-templates.actions";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Alert Templates Actions", () => {
    describe("useGetAlertTemplate", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAlertTemplate());

            expect(result.current.alertTemplate).toBeNull();
            expect(result.current.getAlertTemplate).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessage).toEqual("");
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: { id: 123 },
            });
            const { result, waitFor } = renderHook(() => useGetAlertTemplate());

            await act(async () => {
                const promise = result.current.getAlertTemplate(123);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.alertTemplate).toBeNull();
                expect(result.current.getAlertTemplate).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessage).toEqual("");

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.alertTemplate).toEqual({ id: 123 });
                    expect(result.current.getAlertTemplate).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessage).toEqual("");
                });
            });
        });
    });

    describe("useGetAlertTemplates", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAlertTemplates());

            expect(result.current.alertTemplates).toBeNull();
            expect(result.current.getAlertTemplates).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessage).toEqual("");
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: { id: 123 },
            });
            const { result, waitFor } = renderHook(() =>
                useGetAlertTemplates()
            );

            await act(async () => {
                const promise = result.current.getAlertTemplates();

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.alertTemplates).toBeNull();
                expect(result.current.getAlertTemplates).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessage).toEqual("");

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.alertTemplates).toEqual({ id: 123 });
                    expect(result.current.getAlertTemplates).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessage).toEqual("");
                });
            });
        });
    });
});

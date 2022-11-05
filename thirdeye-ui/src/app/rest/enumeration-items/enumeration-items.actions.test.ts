import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import {
    useGetEnumerationItem,
    useGetEnumerationItems,
} from "./enumeration-items.actions";

const mockEnumerationItem = {
    id: 1,
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Enumeration Items Actions", () => {
    describe("useGetEnumerationItems", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetEnumerationItems());

            expect(result.current.enumerationItems).toBeNull();
            expect(result.current.getEnumerationItems).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: [mockEnumerationItem],
            });
            const { result, waitFor } = renderHook(() =>
                useGetEnumerationItems()
            );
            await act(async () => {
                const promise = result.current.getEnumerationItems({
                    ids: [1],
                });

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.enumerationItems).toBeNull();
                expect(result.current.getEnumerationItems).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.enumerationItems).toEqual([
                        mockEnumerationItem,
                    ]);
                    expect(result.current.getEnumerationItems).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });

    describe("useGetEnumerationItem", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetEnumerationItem());

            expect(result.current.enumerationItem).toBeNull();
            expect(result.current.getEnumerationItem).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: mockEnumerationItem,
            });
            const { result, waitFor } = renderHook(() =>
                useGetEnumerationItem()
            );
            await act(async () => {
                const promise = result.current.getEnumerationItem(1);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.enumerationItem).toBeNull();
                expect(result.current.getEnumerationItem).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.enumerationItem).toEqual(
                        mockEnumerationItem
                    );
                    expect(result.current.getEnumerationItem).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});

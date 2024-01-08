// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom";

const defaultDelay = 50;

export const mockAndDelayResolvedValueOnce = (mock: jest.Mock, value: any, delay: number = defaultDelay) =>
  mock.mockImplementationOnce(() => new Promise((resolve) => setTimeout(() => resolve(value), delay)));

export const mockAndDelayRejectedValueOnce = (mock: jest.Mock, value: any, delay: number = defaultDelay) =>
  mock.mockImplementationOnce(() => new Promise((_, reject) => setTimeout(() => reject(value), delay)));

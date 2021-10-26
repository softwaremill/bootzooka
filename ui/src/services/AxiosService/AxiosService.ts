import { Errors, Type } from "io-ts";
import * as E from 'fp-ts/Either';
import { pipe } from "fp-ts/pipeable";
import reporter from 'io-ts-reporters';
import axios, { AxiosRequestConfig } from "axios";

export async function fetchJson<T, O, I>(
  url: string,
  validator: Type<T, O, I>,
  config: AxiosSecuredRequestConfig = { securedRequest: true },
): Promise<E.Either<Error, T>> {
  try {
    const response = await axios.get(`${url}`, config);
    const json: I = await response.data;
    const result: E.Either<Errors, T> = validator.decode(json);

    return pipe(
      result,
      E.fold(
        () => {
          const messages = reporter.report(result);
          return E.left(new Error(messages.join('\n')));
        },
        (value: T) => E.right(value)),
    )
  } catch (err) {
    return Promise.resolve(E.left(err))
  }
}

export async function sendJson<T, U, O, I>(
  url: string,
  validator: Type<U, O, I>,
  data: T,
  config: AxiosSecuredRequestConfig = { securedRequest: true },
): Promise<E.Either<Error, U>> {
  try {
    const response = await axios.post(`${url}`, data, config);
    const json: I = await response.data;
    const result: E.Either<Errors, U> = validator.decode(json);

    return pipe(
      result,
      E.fold(
        () => {
          const messages = reporter.report(result);
          return E.left(new Error(messages.join('\n')));
        },
        (value: U) => E.right(value)
      )
    )
  } catch (err) {
    return Promise.resolve(E.left(err))
  }
}

type AxiosSecuredRequestConfig = AxiosRequestConfig & { securedRequest?: boolean };

const _securedRequest = (config: AxiosSecuredRequestConfig) => config.securedRequest;

const requestHandler = (request: AxiosRequestConfig) => {
  if (_securedRequest(request)) {
    const apiKey = localStorage.getItem('apiKey');
    request.headers['Authorization'] = `Bearer ${apiKey}`;
  }
  return request;
};

axios.interceptors.request.use(request => requestHandler(request));

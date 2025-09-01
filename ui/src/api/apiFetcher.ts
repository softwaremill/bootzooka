import { ApiKeyState } from '@/hooks/auth';
import { ApiContext } from './apiContext';
import { STORAGE_API_KEY } from '@/consts';

const baseUrl = process.env.REACT_APP_BASE_URL;

export type ErrorWrapper<TError> =
  | TError
  | { status: 'unknown'; payload: string };

export type ApiFetcherOptions<TBody, THeaders, TQueryParams, TPathParams> = {
  url: string;
  method: string;
  body?: TBody;
  headers?: THeaders;
  queryParams?: TQueryParams;
  pathParams?: TPathParams;
  signal?: AbortSignal;
} & ApiContext['fetcherOptions'];

export async function apiFetch<
  TData,
  TError,
  TBody extends {} | FormData | undefined | null,
  THeaders extends {},
  TQueryParams extends {},
  TPathParams extends {},
>({
  url,
  method,
  body,
  headers,
  pathParams,
  queryParams,
  signal,
}: ApiFetcherOptions<
  TBody,
  THeaders,
  TQueryParams,
  TPathParams
>): Promise<TData> {
  let error: ErrorWrapper<TError>;
  try {
    const requestHeaders: HeadersInit = {
      'Content-Type': 'application/json',
      ...headers,
    };

    if (!requestHeaders.Authorization) {
      try {
        const state: ApiKeyState | null = JSON.parse(
          localStorage.getItem(STORAGE_API_KEY) || 'null'
        );
        if (state?.apiKey) {
          requestHeaders.Authorization = `Bearer ${state.apiKey}`;
        }
      } catch (e) {
        console.error(
          'Failed to parse API key from localStorage. Proceeding without an authorization header.',
          e instanceof Error ? e.message : 'Unknown error'
        );
      }
    }

    /**
     * As the fetch API is being used, when multipart/form-data is specified
     * the Content-Type header must be deleted so that the browser can set
     * the correct boundary.
     * https://developer.mozilla.org/en-US/docs/Web/API/FormData/Using_FormData_Objects#sending_files_using_a_formdata_object
     */
    if (
      requestHeaders['Content-Type']
        ?.toLowerCase()
        .includes('multipart/form-data')
    ) {
      delete requestHeaders['Content-Type'];
    }

    const response = await window.fetch(
      `${baseUrl}${resolveUrl(url, queryParams, pathParams)}`,
      {
        signal,
        method: method.toUpperCase(),
        body: body
          ? body instanceof FormData
            ? body
            : JSON.stringify(body)
          : undefined,
        headers: requestHeaders,
      }
    );
    if (!response.ok) {
      try {
        error = await response.json();
      } catch (e) {
        error = {
          status: 'unknown' as const,
          payload:
            e instanceof Error
              ? `Unexpected error (${e.message})`
              : 'Unexpected error',
        };
      }
    } else if (response.headers.get('content-type')?.includes('json')) {
      return await response.json();
    } else {
      // if it is not a json response, assume it is a blob and cast it to TData
      return (await response.blob()) as unknown as TData;
    }
  } catch (e) {
    const errorObject: Error = {
      name: 'unknown' as const,
      message:
        e instanceof Error ? `Network error (${e.message})` : 'Network error',
      stack: e as string,
    };
    throw errorObject;
  }
  throw error;
}

const resolveUrl = (
  url: string,
  queryParams: Record<string, string> = {},
  pathParams: Record<string, string> = {}
) => {
  let query = new URLSearchParams(queryParams).toString();
  if (query) query = `?${query}`;
  return (
    url.replace(/\{\w*\}/g, (key) => pathParams[key.slice(1, -1)] ?? '') + query
  );
};

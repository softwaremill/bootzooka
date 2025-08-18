type ComputeRange<
  N extends number,
  Result extends Array<unknown> = [],
> = Result["length"] extends N
  ? Result
  : ComputeRange<N, [...Result, Result["length"]]>;

export type ClientErrorStatus = Exclude<
  ComputeRange<500>[number],
  ComputeRange<400>[number]
>;
export type ServerErrorStatus = Exclude<
  ComputeRange<600>[number],
  ComputeRange<500>[number]
>;

export function deepMerge<T, U extends T>(target: T, source: U): U {
  const returnType = (target || {}) as U;
  for (const key in source) {
    if (source[key] instanceof Object)
      Object.assign(source[key], deepMerge(returnType[key], source[key]));
  }
  Object.assign(returnType || {}, source);
  return returnType;
}

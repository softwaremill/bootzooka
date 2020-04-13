import { get } from '../Api/api';

export const getAppVersion = async () => get("/admin/version");

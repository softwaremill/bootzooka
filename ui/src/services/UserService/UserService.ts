import axios, { AxiosRequestConfig } from "axios";
import * as Yup from "yup";

const context = "api/v1/user";

const apiKeySchema = Yup.object().required().shape({
  apiKey: Yup.string().required(),
});

const userDetailsSchema = Yup.object().required().shape({
  createdOn: Yup.string().required(),
  email: Yup.string().required(),
  login: Yup.string().required(),
});

const emptySchema = Yup.object().required().shape({});

const registerUser = (params: { login: string; email: string; password: string }) =>
  axios.post(`${context}/register`, params).then(({ data }) => apiKeySchema.validate(data));

const login = (params: { loginOrEmail: string; password: string }) =>
  axios.post(`${context}/login`, { ...params, apiKeyValidHours: 1 }).then(({ data }) => apiKeySchema.validate(data));

const getCurrentUser = (apiKey: string | null) =>
  _securedRequest(apiKey, {
    method: "GET",
    url: context,
  }).then(({ data }) => userDetailsSchema.validate(data));

const changeProfileDetails = (apiKey: string | null, params: { email: string; login: string }) =>
  _securedRequest(apiKey, {
    method: "POST",
    url: context,
    data: params,
  }).then(({ data }) => emptySchema.validate(data));

const changePassword = (apiKey: string | null, params: { currentPassword: string; newPassword: string }) =>
  _securedRequest(apiKey, {
    method: "POST",
    url: `${context}/changepassword`,
    data: params,
  }).then(({ data }) => emptySchema.validate(data));

// const loginWithPasskey = () => {
//   const publicKeyCredentialRequestOptions = {
//     // Server generated challenge
//     challenge: new Uint8Array([117, 61, 252, 231, 191, 49]),
//     // The same RP ID as used during registration
//     rpId: "bootzooka.internal",
//   };

//   navigator.credentials.get({
//     publicKey: publicKeyCredentialRequestOptions,
//     mediation: 'conditional'
//   }).then((credential) => {

//   });
// };

const registerPasskey = (apiKey: string | null, params: {}) =>
  navigator.credentials.create(
    { publicKey: {
      challenge: new Uint8Array([117, 61, 252, 231, 191, 49]),
      rp: {
        name: "Bootzooka",
        id: "bootzooka.internal",
      },
      user: {
        id: new Uint8Array([79, 252, 83, 72, 214, 7, 89, 13]),
        name: "szimano",
        displayName: "szimano",
      },
      pubKeyCredParams: [{alg: -7, type: "public-key"},{alg: -257, type: "public-key"}],
      excludeCredentials: [
      //   {
      //   id: *****,
      //   type: 'public-key',
      //   transports: ['internal'],
      // }
    ],
      authenticatorSelection: {
        authenticatorAttachment: "platform",
        requireResidentKey: true,
      }
    }}).then(( credential ) => {
  //TODO dodać ify na typach
    let cred = credential as PublicKeyCredential;
    let resp = cred?.response as AuthenticatorAttestationResponse;
    console.log("orignal cred")
    console.log(credential)
    console.log("CRED")
    console.log(cred)
    console.log("RESP")
    console.log(resp)
    let attestationData = {
                                                        attestationObject: Array.from(new Uint8Array(resp.attestationObject)),
                                                        clientDataJSON: Array.from(new Uint8Array(resp.clientDataJSON)),
                                                        clientExtensionJSON: JSON.stringify(cred.getClientExtensionResults()),
                                                        transports: resp.getTransports()
                                                      };

    console.log("data:");
    console.log(attestationData);

    _securedRequest(apiKey, {
                                method: "POST",
                                url: `${context}/registerpasskey`,
                                data: JSON.stringify(attestationData),
                             })
  .then(({ data }) => emptySchema.validate(data))});

const _securedRequest = (apiKey: string | null, config: AxiosRequestConfig) =>
  axios.request({
    headers: {
      Authorization: `Bearer ${apiKey}`,
    },
    ...config,
  });

export const userService = {
  registerUser,
  login,
  getCurrentUser,
  changeProfileDetails,
  changePassword,
  registerPasskey,
};

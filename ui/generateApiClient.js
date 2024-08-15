const OpenAPIClientAxios = require('openapi-client-axios').default;
const fs = require('fs');
const path = require('path');

const generateApiClient = async () => {
  const apiSpecPath = 'http://localhost:3000/openapi.yaml';
  const outputPath = path.resolve(__dirname, './openapi/api.js');


  const api = new OpenAPIClientAxios({
    definition: apiSpecPath,
    axiosConfigDefaults: {
      baseURL: 'http://localhost:8080/api/v1/',
    },
  });

  await api.init();

  async function createUser() {
    const client = await api.getClient();
    const data = {
      "login": "test",
      "email": "test@test.com",
      "password": "pass"
    }
    const res = await client.postUserRegister(null, data);
    console.log("User created", res.data);
  }

  await createUser()
};

module.exports = generateApiClient;





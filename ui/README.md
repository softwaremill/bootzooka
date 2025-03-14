This project was bootstrapped with [Vite](https://vite.dev/).

## Available Scripts

In the project directory, you can run:

### `yarn start`

Runs the app in the development mode.<br />
Open [http://localhost:8081](http://localhost:8081) to view it in the browser.

The page will reload if you make edits.<br />
You will also see any lint errors in the console.

#### API client & associated types

Before running `yarn start`, make sure to run `sbt "backend/generateOpenAPIDescription"` in the project's root directory. This command will generate the `<project_root>/backend/target/openapi.yaml` file.

Type-safe React Query hooks are generated upon UI application start (`yarn start`), based on the `openapi.yaml` contents.

These files are:

- `src/api/{namespace}Fetcher.ts` - defines a function that will make requests to your API.
- `src/api/{namespace}Context.ts` - the context that provides `{namespace}Fetcher` to other components.
- `src/api/{namespace}Components.ts` - generated React Query components (if you selected React Query as part of initialization).
- `src/api/{namespace}Schemas.ts` - the generated Typescript types from the provided Open API schemas.

A file watch is engaged, re-generating types on each change to the `<project_root>/backend/target/openapi.yaml` file.

### `yarn test`

Launches the test runner in the interactive watch mode.<br />
See the section about [running tests](https://vitest.dev/guide/) for more information.

### `yarn build`

Builds the app for production to the `dist` folder.<br />
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.<br />
Your app is ready to be deployed!

See the section about [deployment](https://vite.dev/guide/static-deploy) for more information.

## Learn More

You can learn more in the [Vite documentation](https://vite.dev/).

To learn React, check out the [React documentation](https://react.dev/).

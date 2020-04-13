import React from 'react';
import Footer from './Footer';
import { render } from "@testing-library/react";
import { Right } from "ts-matches";

const build = 'abc-123';
const date = '2018-07-16 15:55';

const getVersion = jest.fn();
getVersion.mockReturnValue(Promise.resolve({
  data: {
    build,
    date
  }
}));

const version = Right.of({ buildDate: "v1.0", buildSha: "aacd1724ffa61f47d5af67d3b05799da7991c4d7" });

it('renders version component', () => {
  render(
    <Footer version={version}/>
  )
});

describe('structure', () => {
  it('should fetch version info', () => {
    //const wrapper = shallow(<Footer versionService = { versionService }/>);
    expect(getVersion.mock.calls.length).toBe(1);
  });
});

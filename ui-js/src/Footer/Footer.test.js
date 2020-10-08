import React from 'react';
import { shallow } from 'enzyme';
import Footer from './Footer';

const build = 'abc-123';
const date = '2018-07-16 15:55';

const getVersion = jest.fn();
getVersion.mockReturnValue(Promise.resolve({
  data: {
    build,
    date
  }
}));

const versionService = {
  getVersion
};

describe('structure', () => {
  it('should fetch version info', () => {
    const wrapper = shallow(<Footer versionService={versionService} />);
    expect(getVersion.mock.calls.length).toBe(1);
  });
});

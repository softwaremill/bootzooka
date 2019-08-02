import React, { Component } from 'react';
import PropTypes from 'prop-types';

class Footer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      version: '',
    };
  }

  async componentDidMount() {
    try {
      const { data } = await this.props.versionService.getVersion();
      const { buildDate, buildSha } = data;
      this.setState({ version: `${buildDate}, ${buildSha}` });
    } catch (error) {
      console.error(error);
    }
  }

  render() {
    return (
      <div className="Footer">
        <p>Bootzooka - application scaffolding by <span><a href="http://softwaremill.com">SoftwareMill</a></span>,
          sources available on <span><a href="https://github.com/softwaremill/bootzooka/">GitHub</a></span>
        </p>
        <p>
          { this.state.version }
        </p>
      </div>
    );
  }
}

Footer.propTypes = {
  versionService: PropTypes.shape({
    getVersion: PropTypes.func.isRequired,
  }),
};

export default Footer;

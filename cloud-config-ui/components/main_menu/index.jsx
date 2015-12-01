import React from "react";
import logo from "../../images/logo.png";
import $ from "jquery";

const MainMenu = React.createClass({
  componentDidMount () {
    $('.ui.dropdown').dropdown({
      on: 'hover'
    });
  },

  render() {
    return (
      <div className="ui inverted fixed top menu borderless">
        <div className="item">
          <a className="ui logo image" href="/">
            <img src={logo}/>
          </a>
          <a href="/">
            <b>Cloud Config Center</b>
          </a>
        </div>
        <div className="ui dropdown item">
          New
          <i className="dropdown icon"></i>
          <div className="menu">
            <a className="item">New Tenant</a>
            <a className="item">New Property</a>
            <a className="item">New Resource</a>
          </div>
        </div>
        <a className="item">Import</a>
        <a className="item">Export</a>
      </div>
    );
  }
});

export default MainMenu;

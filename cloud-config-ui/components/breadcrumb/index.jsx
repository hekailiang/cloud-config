import React from 'react';
import emitter from '../utils/emitter';

const Breadcrumb = React.createClass({
  getInitialState() {
    return {locations: []};
  },

  componentDidMount () {
    emitter.on("menu.click", (e) => {
      this.setState( {locations: this.state.locations.concat([e.name])} );
    });
    emitter.on("menu.uplevel", () => {
      this.state.locations.pop();
      this.setState( {locations: this.state.locations} );
    });
  },

  componentWillUnmount () {
    emitter.removeListener('menu.click');
    emitter.removeListener('menu.uplevel');
  },

  handleClick(i) {
    emitter.emit("breadcrumb.click", i);
    this.setState( {locations: this.state.locations.slice(0, i+1)} );
  },

  render() {
    var items = [];
    var locSize = this.state.locations.length;
    this.state.locations.map((loc, i) => {
      items.push(
        <div className="divider" key={`d_${i}`}> / </div>
      );
      if (locSize === i+1) {
        items.push(
          <div className="active section" key={i}>
            {loc}
          </div>
        );
      } else {
        items.push(
          <a className="section" key={i} onClick={this.handleClick.bind(null, i)}>
            {loc}
          </a>
        );
      }
    });
    return (
      <div className="ui breadcrumb">
        {"Location: "}
        {items}
      </div>
    );
  }
});

export default Breadcrumb;

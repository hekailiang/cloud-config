import React from 'react';
import emitter from '../utils/emitter'
import PropertyEditor from './property_editor';
import ResourceEditor from './resource_editor';
import reqwest from 'reqwest';

export default React.createClass({
  getInitialState() {
    return {dataSource: {}, selectedNode: {}};
  },

  componentDidMount () {
    emitter.on("path.change", (selectedNode) => {
      if(selectedNode.path && selectedNode.type === "file") {
        reqwest({
          url: 'api/data?path='+selectedNode.path+'&name='+selectedNode.name,
          type: 'json',
        }).then(reps => {
          this.setState(
            {dataSource: reps, selectedNode: selectedNode}
          );
        }, (err, msg) => {
          console.log(msg);
        });
      } else {
        this.setState({ dataSource: {}, selectedNode: {} });
      }
    });
  },

  componentWillUnmount () {
    emitter.removeListener('path.change');
  },

  render() {
    if( this.state.dataSource.__type__ && this.state.selectedNode.type == "file" && this.state.selectedNode.name != ".settings" ) {
      return <ResourceEditor path={this.state.selectedNode.path} dataSource={this.state.dataSource}/>;
    } else if( this.state.selectedNode.type == "file" ) {
      return <PropertyEditor path={this.state.selectedNode.path} dataSource={this.state.dataSource}/>;
    } else {
      return (
        <div>No Editor</div>
      );
    }
  }
});

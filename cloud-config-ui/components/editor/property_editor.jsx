import React from 'react';
import SaveHandler from './save_handler';

const PropertyEditor = React.createClass({

  mixins: [SaveHandler],

  getInitialState() {
    return { properties: this.props.dataSource, dirty: false };
  },

  afterSave() {
    this.setState({properties: this.state.properties, dirty: false });
  },

  handleAdd() {
    var name = this.refs.newPropName.value.trim();
    var value = this.refs.newPropValue.value.trim();
    if (!name || !value) {
        return;
    }
    this.state.properties[name] = value;
    this.setState({properties: this.state.properties, dirty: true });

    this.refs.newPropName.value = '';
    this.refs.newPropValue.value = '';
    return;
  },

  handleRemove(key) {
    delete this.state.properties[key];
    this.setState({properties: this.state.properties, dirty: true });
  },

  handleKey(e) {
    if( e.keyCode === 13 /*ENTER*/ ) {
      this.handleAdd();
      this.refs.newPropName.focus();
    }
  },

  renderFormBody() {
    var rows = [];
    for (var key in this.state.properties) {
      if (this.state.properties.hasOwnProperty(key)) {
        var val = this.state.properties[key];
        rows.push(
          <div className="inline fields" key={key}>
            <div className="disabled six wide field">
              <input type="text" defaultValue={key}/>
            </div>
            <div className="six wide field">
              <input type="text" name={key} ref={(ref)=>{if(ref!=null) this.fieldRefs.push(ref)}} defaultValue={val}/>
            </div>
            <div className="one wide field">
              <i className="remove circle icon" onClick={this.handleRemove.bind(null, key)} style={{cursor: "pointer"}}/>
            </div>
          </div>
        );
      }
    }
    return rows;
  },

  render() {
    return (
      <div>
        <div className="ui form" ref="content">
          <h4 className="ui header">System Properties</h4>
          <p>{this.props.path}</p>
          {this.renderFormBody()}
        </div>
        <div className="ui form" style={{marginTop: "1em"}} onKeyDown={this.handleKey}>
          <div className="inline fields">
            <div className="six wide field">
              <input type="text" ref="newPropName" placeholder="New Property Name"/>
            </div>
            <div className="six wide field">
              <input type="text" ref="newPropValue" placeholder="New Property Value"/>
            </div>
            <div className="one wide field">
              <button className="ui green button" onClick={this.handleAdd}>Add</button>
            </div>
          </div>
        </div>
        <button className="ui primary button" style={{marginBottom: "1em"}} onClick={this.handleSave}>
          {(this.state.dirty ? "Submit *" : "Submit")}
        </button>
      </div>
    );
  }

});

export default PropertyEditor;

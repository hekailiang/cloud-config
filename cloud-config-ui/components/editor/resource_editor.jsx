import React from 'react';
import SaveHandler from './save_handler';

const ResourceEditor = React.createClass({
  mixins: [SaveHandler],
  
  afterSave(resp) {
    console.log("data save - "+resp);
  },

  renderFormBody() {
    var fields=[], sortedKeys = [];
    var formFields = this.props.dataSource.properties;
    for (var formfield in formFields) {
      if (formFields.hasOwnProperty(formfield)) {
        sortedKeys.push({key: formfield, order: formFields[formfield].order});
      }
    }
    sortedKeys.sort(function(a, b){
      return a.order - b.order;
    });
    for (var i=0; i<sortedKeys.length; i++) {
      var key = sortedKeys[i].key;
      var fieldMeta = formFields[key];
      fields.push(
        <div className="field" key={key}>
          <label>{fieldMeta.label}</label>
          <input
            type="text"
            name={key}
            placeholder={fieldMeta.placeholder}
            defaultValue={fieldMeta.defaultValue}
            ref={(ref)=>this.fieldRefs.push(ref)} />
        </div>
      );
    }
    return fields;
  },

  render() {
    return (
      <div className="ui form">
        <h4 className="ui header">
          {this.props.dataSource.title}
        </h4>
        <p>
          {this.props.path}
          <i className="dropdown icon" style={{cursor: "pointer"}}/>
        </p>
        {this.renderFormBody()}
        <button className="ui primary button" onClick={this.handleSave}>
          Submit
        </button>
        <button className="ui secondary button">
          Test Config
        </button>
      </div>
    );
  }
});

export default ResourceEditor;

import React from 'react';
import emitter from '../utils/emitter';
import reqwest from 'reqwest';

const Tree = React.createClass({
  getInitialState() {
    return {dataSource: {}, selectPath:[], selected: -1};
  },

  getCurrentDataSource() {
    let currentDataSource = this.state.dataSource;
    if(this.state.selectPath.length > 0) {
      this.state.selectPath.map(name => {
        currentDataSource = currentDataSource.children.filter(cds => {
          return cds.name == name;
        })[0];
      });
    }
    return currentDataSource;
  },

  componentDidMount () {
    reqwest({
      url: 'api/structure',
      type: 'json',
    }).then(reps => {
      this.setState(
        {dataSource: reps, selectPath:[], selected: -1}
      );
      emitter.emit("menu.click", {
        name: reps.name,
        path: reps.path || ""
      });
    }, (err, msg) => {
      console.log(msg);
    });
    emitter.on("breadcrumb.click", (idx) => {
      this.setState( {
        dataSource: this.state.dataSource,
        selectPath: this.state.selectPath.slice(0, idx),
        selected: -1
      } );
    });
  },

  componentWillUnmount () {
    emitter.removeListener('breadcrumb.click');
  },

  componentDidUpdate(prevProps, prevState) {
    var ds = this.state.selected>=0 ? this.getCurrentDataSource().children[this.state.selected] : this.state.dataSource;
    emitter.emit("path.change", {path: ds.path || "", type: ds.type, name: ds.name});
  },

  handleClick(i) {
    let currentDataSource = this.getCurrentDataSource();
    let selectedDataSource = currentDataSource.children[i];
    if (selectedDataSource.type === "folder") {
      this.state.selectPath.push(selectedDataSource.name);
      this.setState({
        dataSource: this.state.dataSource,
        selectPath: this.state.selectPath,
        selected: -1
      });
      emitter.emit("menu.click", {
        name: selectedDataSource.name,
        path: selectedDataSource.path || ""
      });
    } else {
      let selectedIdx = (this.state.selected === i) ? -1 : i;
      this.setState({
        dataSource: this.state.dataSource,
        selectPath: this.state.selectPath,
        selected: selectedIdx
      });
    }
  },

  handleClickUpLevel() {
    if (this.state.selectPath && this.state.selectPath.length>0) {
      this.state.selectPath.pop();
      this.setState({
        dataSource: this.state.dataSource,
        selectPath: this.state.selectPath,
        selected: -1
      });
      emitter.emit("menu.uplevel");
    }
  },

  handleKeyPress(e) {
    if( e.keyCode === 13 /*ENTER*/ ) {
      this.handleAdd();
    }
  },

  handleAdd() {
    const currentDataSource = this.getCurrentDataSource();
    const newValue = this.refs.newItem.value.trim();
    const newPath = this.state.selected>=0 ?
      currentDataSource.children[this.state.selected].path :
      currentDataSource.path;
    reqwest({
      url: 'api/structure',
      headers: {
        Accept: 'text/plain'
      },
      contentType: 'application/json',
      method: 'post',
      data: JSON.stringify( {path: newPath, value: newValue} )
    }).then( resp => {
      this.reload();
    }, (err, msg) => {
      console.log(`Add "${newValue}" node under "${this.state.dataSource.path}" failed: "${msg}"`);
    });
    this.refs.newItem.value = '';
  },

  reload() {
    reqwest({
      url: 'api/structure',
      type: 'json',
    }).then(reps => {
      var newDataSource = reps;
      this.setState({
        dataSource: newDataSource,
        selectPath: this.state.selectPath,
        selected: this.state.selected
        }
      );
    }, (err, msg) => {
      console.log(msg);
    });
  },

  handleRemove() {
    if(this.state.selected >= 0) {
      const removePath = this.getCurrentDataSource().children[this.state.selected].path;
      reqwest({
        url: 'api/structure?path='+removePath,
        headers: {
          Accept: 'text/plain'
        },
        method: 'delete'
      }).then(reps => {
        this.state.selected = -1;
        this.reload();
      }, (err, msg) => {
        console.log(msg);
      });
    }
  },

  render() {
    return (
      <div className="ui list">
        <div className="ui vertical fluid menu">
          <a className="item" key=".." onClick={this.handleClickUpLevel}>
            . .
            <i className="level up icon"/>
          </a>
          {
            (this.getCurrentDataSource().children || []).map( (node, i) => {
              var className = "item";
              if(this.state.selected === i && node.type === "file") {
                className += " active teal"
              }
              return (
                <a className={className} key={i} onClick={this.handleClick.bind(null, i)}>
                  {node.name}
                  <i className={`${node.type} icon`}/>
                </a>
              );
            })
          }
          <div className="item">
            <div className="ui transparent icon input" onKeyDown={this.handleKeyPress}>
              <input type="text" ref="newItem" placeholder="New item..."/>
              <div className="ui right dropdown">
                . . .
                <div className="menu">
                  <div className="item" onClick={this.handleAdd}>
                    <i className="add square icon"></i>
                    Add
                  </div>
                  <div className="item" onClick={this.handleRemove}>
                    <i className="trash outline icon"></i>
                    Remove
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
});

export default Tree;

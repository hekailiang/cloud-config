import React from 'react';
import Breadcrumb from '../breadcrumb';
import MainMenu from '../main_menu';
import Tree from '../tree';
import Editor from '../editor';
import $ from "jquery";
import 'imports?$=jquery,jQuery=jquery!semantic-ui-components/dropdown';
import 'imports?$=jquery,jQuery=jquery!semantic-ui-components/transition';
import 'imports?$=jquery,jQuery=jquery!semantic-ui-components/checkbox';

import {} from './style';

const Main = () => {
  return (
    <div>
      <MainMenu/>
      <div className="main">
        <Breadcrumb/>
        <div className="ui divider"></div>
        <div className="ui grid">
          <div className="four wide column">
            <Tree/>
          </div>
          <div className="twelve wide column">
            <Editor/>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Main;

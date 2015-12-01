import React from 'react';
import ReactDOM from 'react-dom';
import Main from './components/main';

function run() {
  ReactDOM.render(
    <Main/>,
    document.getElementById('app')
  );
}

const loadedStates = ['complete', 'loaded', 'interactive'];

if (loadedStates.includes(document.readyState) && document.body) {
  run();
} else {
  window.addEventListener('DOMContentLoaded', run, false);
}

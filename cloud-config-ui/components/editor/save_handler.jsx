import reqwest from 'reqwest';

export default {
  componentWillMount() {
    this.fieldRefs = [];
  },

  componentWillUpdate() {
    this.fieldRefs = [];
  },

  handleSave() {
    let data = {};
    this.fieldRefs.map(ref => {
      if(ref) {
        data[ref.name] = ref.value;
      }
    });
    reqwest({
      url: 'api/data?path='+this.props.path,
      headers: {
        Accept: 'text/plain'
      },
      contentType: 'application/json',
      method: 'post',
      data: JSON.stringify(data, null, 2)
    }).then( resp => {
      this.afterSave(resp);
    }, (err, msg) => {
      console.log("data save - "+msg);
    });
  }
};

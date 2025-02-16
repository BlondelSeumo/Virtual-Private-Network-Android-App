import * as React from 'react';
import ReactDOM from 'react-dom';
import {IMGUR, imgurKey} from "@app/configs";
import Axios from "axios";
import {LoadingPage} from "@app/components/loading";

const loadScript = require('load-script');

const URL = 'https://cdn.ckeditor.com/4.11.3/full-all/ckeditor.js';

// interface IEditorProps {
//   onChange: Function;
//   defaultValue: string;
//   height?: number;
//   nullabled?: boolean;
// }
//
// interface IStates {
//   isScriptLoaded: boolean;
// }

class TextEditor extends React.PureComponent {
  editorInstance = undefined;
  unmounting = false;
  isOnChange = false;
  nameCKEditor = 'SHD' + Math.random().toString();
  fileSelector = undefined;
  editor = undefined;

  constructor(props) {
    super(props);
    this.state = {
      isScriptLoaded: false,
      isUploadImage: false,
      tmpImage: undefined,
      editor: undefined,
      imageBase64: undefined,
      isLoading: false
    };
  }

  getBase64 = (img) => {
    const reader = new FileReader();
    reader.addEventListener('load', () => this.setState({imageBase64: reader.result}));
    reader.readAsDataURL(img);
  }

  onUpload = (path) => {
    this.editor?.insertHtml(`<p><img src="${path}" class='h-auto' style="width: 400px"/></p>`);
    setTimeout(() => {
      const dom =document.getElementById("editor-loading")
      dom.style.display = "none"
      this.editor = undefined
    }, 1000)
  }

  componentDidMount() {
    this.fileSelector = this.buildFileSelector();
    if (!this.state.isScriptLoaded) {
      loadScript(URL, this.onLoad);
    } else {
      this.onLoad();
    }
  }

  buildFileSelector = () => {
    const fileSelector = document.createElement('input');
    fileSelector.setAttribute('type', 'file');
    fileSelector.setAttribute('name', 'file');
    fileSelector.onchange = (e) => {
      this.onChoose(e);
      fileSelector.value = ''
    };

    return fileSelector;
  };

  handleFileSelect = () => {
    this.fileSelector?.click();
  };

  onChoose = (event) => {
    const {
      cb,
      errorMessage,
      fileType
    } = this.props;
    const file = event.target.files[0];

    const dom =document.getElementById("editor-loading")
    dom.style.display = "block"

    const formData = new FormData();

    formData.append('image', file, file?.name);
    formData.append('album', imgurKey?.id3);

    Axios.post(`${IMGUR}image`, formData, {
      headers: {
        Authorization: `Client-ID ${imgurKey?.id1}`,
      },
    })
      .then((result: any) => {
        const data = result?.data?.data || {};
        const url = data?.link || ""
        this.onUpload(url)
      });
  };

  componentDidUpdate() {
    const editor = this.editorInstance;
    if (editor && (editor.getData() !== this.props.defaultValue) && !this.isOnChange) {
      editor.setData(this.props.defaultValue);
    }

    if (editor && (editor.getData() !== this.props.defaultValue) && this.isOnChange && this.props.nullabled) {
      editor.setData(this.props.defaultValue);
    }
  }

  onLoad = () => {
    if (this.unmounting) return;

    this.setState({
      isScriptLoaded: true,
    });

    // @ts-ignore
    if (!window.CKEDITOR) {
      console.error('CKEditor not found');
      return;
    }

    // @ts-ignore
    if (!window.CKEDITOR.plugins.get('insertimage')) {
      // @ts-ignore
      window.CKEDITOR.plugins.add('insertimage', {
        init: (editor) => {
          editor.addCommand('insertimage', {
            exec: (editor) => {
              this.editor = editor;
              this.handleFileSelect()
            },
          });
          editor.ui.addButton('insertimage', {
            label: 'Insert Image',
            command: 'insertimage',
            icon: 'image',
          });
        },
      });
    }

    // @ts-ignore
    this.editorInstance = window.CKEDITOR.appendTo(
      ReactDOM.findDOMNode(this),
      this.config(),
      this.props.defaultValue,
    );

    this.editorInstance.on('change', (data) => {
      this.isOnChange = true;
      this.props.onChange(data.editor.getData() || '');
    });
  }

  config = () => ({
    skin: 'moono-lisa',
    removeFormatAttributes: 'class,lang,hspace,valign',
    extraAllowedContent: 'i;span;ul;li;table;td;style;*[id];*(*);*{*}',
    allowedContent: true,
    htmlEncodeOutput: false,
    entities: false,
    toolbar: [
      {
        name: 'document', groups: ['mode', 'document', 'doctools'],
        items: ['Undo', 'Redo'],
      },
      {
        name: 'basicstyles', groups: ['basicstyles', 'cleanup'],
        items: ['Bold', 'Italic', 'Underline', 'Strike', 'RemoveFormat'],
      },
      {name: 'styles', items: ['Styles', 'Format', 'Font', 'FontSize']},
      {name: 'colors', items: ['TextColor', 'BGColor']},
      {
        name: 'paragraph', groups: ['list', 'blocks', 'align'], items: ['NumberedList', 'BulletedList', '-',
          'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'],
      },
      {name: 'links', items: ['Link', 'Unlink']},
      {
        name: 'insert', items: ['Table', 'insertimage'],
      },
      {
        name: 'document', groups: ['mode', 'document', 'doctools'],
        items: ['Source'],
      },
    ],
    extraPlugins: 'insertimage',
    height: this.props.height || 400,
  })

  componentWillUnmount() {
    this.unmounting = true;

    this.setState({
      editor: undefined, isUploadImage: false, imageBase64: "/"
    })
  }

  render() {
    return (
      <>
        <div className="shd-content-editor" id={this.nameCKEditor}>
        </div>
        <div id="editor-loading" style={{ display: "none" }}>
          {<LoadingPage/>}
        </div>
      </>
    );
  }
}

export default TextEditor;

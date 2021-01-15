import React, {useState} from 'react';
import axios from 'axios';
import Router from 'next/router';

const CreatePost = ({setPostCreateVisible}) => {
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [isTitleEmpty, setIsTitleEmpty] = useState(false);
  const [isBodyEmpty, setIsBodyEmpty] = useState(false);
  const closeCreatePost = (event) => {
    event.preventDefault();
    setPostCreateVisible(false);
  }
  const onSubmit = async (event) => {
    event.preventDefault();
    setIsTitleEmpty(title === "");
    setIsBodyEmpty(body === "");
    if (!isTitleEmpty && !isBodyEmpty) {
      try {
        const response = await axios.post("/api/posts", {title, body});
        setPostCreateVisible(false);
        Router.push("/");
      }
      catch (err) {}
    }
  }
  const buttonStyle = {
    position: "absolute",
    top: '2%',
    right: '1%',
    width: "25px",
    height: "25px"
  };
  const divStyle = {
    padding: "20px 20px 20px 20px"
  }
  return (
    <div className="col-8 border border-secondary rounded mt-10" style={divStyle}>
      <button className="btn btn-outline-danger btn-sm close" style={buttonStyle} onClick={closeCreatePost}>
        <span aria-hidden="true">&times;</span>
      </button>
      <form onSubmit={onSubmit}>
        <div className="form-group">
          <label>Title:</label>
          <input value={title} onChange={e => setTitle(e.target.value)} className="form-control"/>
          {isTitleEmpty && <div className="alert alert-danger mt-3">Title cannot be empty!</div>}
        </div>
        <div className="form-group">
          <label>Body:</label>
          <textarea value={body} onChange={e => setBody(e.target.value)} className="form-control"/>
          {isBodyEmpty && <div className="alert alert-danger mt-3">Body cannot be empty!</div>}
        </div>
        <button className="btn btn-primary float-sm-right">Post</button>
      </form>
    </div>
  );
};

export default CreatePost;
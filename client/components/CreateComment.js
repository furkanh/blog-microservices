import { useState } from "react";
import axios from 'axios';
import Router from 'next/router';

const CreateComment = ({postId}) => {
  const [comment, setComment] = useState("");
  const [isCommentEmpty, setIsCommentEmpty] = useState(false);
  const onSubmit = async (event) => {
    event.preventDefault();
    setIsCommentEmpty(comment === "");
    if (!isCommentEmpty) {
      try {
        const response = await axios.post("/api/comments?postId="+postId, {body: comment});
        setComment("");
        Router.push("/posts/"+postId);
      }
      catch (err) {}
    }
  }
  return (
    <div>
      <form onSubmit={onSubmit}>
        <div className="form-group">
          <label>Comment:</label>
          <input value={comment} onChange={e => setComment(e.target.value)} className="form-control"/>
          {isCommentEmpty && <div className="alert alert-danger mt-3">Comment cannot be empty!</div>}
        </div>
        <button className="btn btn-primary float-sm-right">Comment</button>
      </form>
    </div>
  );
};

export default CreateComment;
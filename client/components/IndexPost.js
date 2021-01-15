import React, {useState} from 'react';
import axios from 'axios';
import Router from 'next/router';

const IndexPost = ({post}) => {
  const divStyle = {
    padding: "20px 20px 20px 20px"
  }
  return (
    <div className="col-8 mt-5 border border-secondary rounded" style={divStyle}>
      <div className="container">
        <div className="row">
          <h3>{post.title}</h3>
        </div>
        <div className="row">
          {post.body}
        </div>
        <div className="row float-sm-right">
          {post.numberOfComments} comments
        </div>
      </div>
    </div>
  );
};

export default IndexPost;
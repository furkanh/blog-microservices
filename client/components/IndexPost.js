import React, {useState} from 'react';
import axios from 'axios';
import Router from 'next/router';
import Link from 'next/link';

const IndexPost = ({post}) => {
  const divStyle = {
    padding: "20px 20px 20px 20px"
  }
  return (
    <div className="col-8 mt-5 border border-secondary rounded" style={divStyle}>
      <div className="container">
        <div className="row">
          <Link href={'/posts/' + post.id}>
            <a><h3>{post.title}</h3></a>
          </Link>
        </div>
        <div className="row">
          {post.body}
        </div>
        <div className="row float-sm-right">
          <Link href={'/posts/' + post.id}>
            <a>{post.numberOfComments} comments</a>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default IndexPost;
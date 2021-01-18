import CommentList from "../../components/CommentList";
import CreateComment from "../../components/CreateComment";

const Post = ({post, comments, setPostCreateButtonVisible}) => {
  setPostCreateButtonVisible(false);
  return (
    <div className="container">
      <div className="row justify-content-md-center">
        <div className="col-8 mt-5">
          <div className="container">
            <div className="row">
              <h2>{post.title}</h2>
            </div>
            <div className="row">
              {post.body}
            </div>
          </div>
        </div>
      </div>
      <div className="row justify-content-md-center">
        <div className="col-8 mt-5">
          <CreateComment postId={post.id}/>
        </div>
      </div>
      <div className="row justify-content-md-center">
        <div className="col-8 mt-5">
          <CommentList comments={comments}/>
        </div>
      </div>
    </div>
  );
};

Post.getInitialProps = async (context, client) => {
  const { postId } = context.query;
  let post = {};
  let comments = [];
  try {
    let response = await client.get("/api/posts/" + postId);
    post = response.data;
    response = await client.get("/api/comments?postId="+postId);
    comments = response.data;
  }
  catch (err) {}
  return {post, comments};
};

export default Post;
import CommentList from "../../components/CommentList";

const Post = ({post}) => {
  return (
    <div className="container">
      <div className="row">
        <h2>{post.title}</h2>
      </div>
      <div className="row">
        {post.body}
      </div>
      <CommentList comments={post.comments}/>
    </div>
  );
};

Post.getInitialProps = async (context, client) => {
  const { postId } = context.query;
  let post = {};
  try {
    const response = await client.get("/api/posts/" + postId);
    post = response.data;
    post.comments = await client.get("/api/comments?postId="+postId).data;
  }
  catch (err) {}
  return {post};
};

export default Post;
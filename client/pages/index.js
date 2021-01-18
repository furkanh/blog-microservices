import CreatePost from "../components/CreatePost";
import IndexPostFeed from "../components/IndexPostFeed";


const Index = ( { postCreateVisible, setPostCreateVisible, posts, setPostCreateButtonVisible} ) => {
  const style = {
    padding: "20px 10px 10px 10px"
  }
  setPostCreateButtonVisible(true);
  return (
    <div className="container" style={style}>
      <div className="row justify-content-md-center">
        { postCreateVisible && <CreatePost postCreateVisible={postCreateVisible} setPostCreateVisible={setPostCreateVisible}/> }
      </div>
      <div className="row justify-content-md-center">
        { posts.length!=0 && <IndexPostFeed posts={posts}/>}
      </div>
    </div>
  );
};

Index.getInitialProps = async (context, client) => {
  let posts = [];
  try {
    const response = await client.get("/api/feed");
    posts = response.data;
  }
  catch (err) {}
  return {posts};
};

export default Index;
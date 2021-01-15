import IndexPost from "./IndexPost";

const IndexPostFeed = ({posts}) => {
  return (
    <div className="container">
      {posts.map(item => {
        return (
          <div className="row justify-content-md-center">
            <IndexPost key={item.id} post={item}/>
          </div>
        );
      })}
    </div>
  );
};

export default IndexPostFeed;
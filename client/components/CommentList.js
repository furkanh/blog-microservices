import axios from 'axios';

const CommentList = ({comments}) => {
  return (
    <div className="contrainer">
      {comments && comments.length != 0 && comments.map(item => {
          return (
            <div key={item.id} className="row justify-content-md-center">
              {item}
            </div>
          );
        })}
      {(!comments || comments.length == 0) && <div>No comments!</div>}
    </div>
  );
}

export default CommentList;
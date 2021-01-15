import Link from 'next/link';

const SiteNavbar = ({ setPostCreateVisible }) => {
  const createPost = (event) => {
    event.preventDefault();
    setPostCreateVisible(true);
  };
  return (
    <nav className="navbar navbar-dark bg-dark">
      <Link href="/"><a className="navbar-brand">blog app</a></Link>
      <div className="d-flex justify-content-end">
        <ul className="nav d-flex align-items-center">
          <li className="nav-item">
            <button className="btn btn-primary" onClick={createPost}>
              Create Post
            </button>
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default SiteNavbar;
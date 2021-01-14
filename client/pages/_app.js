import 'bootstrap/dist/css/bootstrap.css';
import { useState } from 'react';
import SiteNavbar from '../components/SiteNavbar';

const App = ({ Component, pageProps }) => {
  const [postCreateVisible, setPostCreateVisible] = useState(false);
  return (
    <div>
      <SiteNavbar setPostCreateVisible={setPostCreateVisible}/>
      <Component
        {...pageProps}
        postCreateVisible={postCreateVisible}
        setPostCreateVisible={setPostCreateVisible}
      />
    </div>
  );
};

export default App;
import 'bootstrap/dist/css/bootstrap.css';
import { useState } from 'react';
import SiteNavbar from '../components/SiteNavbar';
import buildClient from '../api/build-client';

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

App.getInitialProps = async (context) => {
  let pageProps = {};
  const client = buildClient(context.ctx);
  if (context.Component.getInitialProps) {
    pageProps = await context.Component.getInitialProps(context.ctx, client);
  }
  return { pageProps };
};

export default App;
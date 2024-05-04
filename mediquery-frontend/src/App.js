import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './App.css';
import SearchPage from './views/SearchPage/SearchPage';
import SearchResultPage from './views/SearchResultPage/SearchResultPage'; // Assuming you have a SearchResultPage component
import NotFound from './views/NotFound/NotFound';

function App() {
  return (
    <div className="App">
      <Router>
        <Routes>
          <Route  path="/" Component={SearchPage}/>
          <Route exact  path="/search-results" Component={SearchResultPage}/>
          <Route exact  path="/not-found" Component={NotFound}/>
        </Routes>
      </Router>
    </div>
  );
}

export default App;

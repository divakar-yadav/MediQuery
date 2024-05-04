import React, { useState, useEffect } from 'react';
import './SearchPage.css';
import search from '../../assets/images/search.svg';
import axios from 'axios';
import { Link } from 'react-router-dom';

const SearchPage = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [lookupType, setLookupType] = useState('Researcher'); // Default lookup type
  const [searchHistory, setSearchHistory] = useState([]);
  const [isDropDownOpen, setIsDropDownOpen] = useState(false);
  const [startYear, setStartYear] = useState('');
  const [endYear, setEndYear] = useState('');

  useEffect(() => {
    const storedSearchHistory = localStorage.getItem('searchHistory');
    if (storedSearchHistory) {
      setSearchHistory(JSON.parse(storedSearchHistory));
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('searchHistory', JSON.stringify(searchHistory));
  }, [searchHistory]);

  const handleInputChange = async (event) => {
    setSearchTerm(event.target.value);
    if(event.target.value.length === 0){
      setIsDropDownOpen(false);
    }
    if(event.target.value.length > 4){
      fetchSearchResults(event.target.value);
      setIsDropDownOpen(true);
    }
  };

  const handleYearChange = (setter) => (event) => {
    setter(event.target.value);
  };

  const fetchSearchResults = async (term) => {
    try {
      const response = await axios.get(`http://localhost:8080/suggest?query=${term}`, {
        params: {
          searchTerm: term,
          lookupType: lookupType,
          startYear: startYear,
          endYear: endYear
        }
      });
      if (response.status === 200) {
        console.log(response,"-----response-----");
        setSearchResults(response.data); // Update search results
      } else {
        console.error('Failed to fetch search results');
      }
    } catch (error) {
      console.error('Error fetching search results:', error);
    }
  };

  const handleDeleteHistory = (index) => {
    setSearchHistory(prevHistory => prevHistory.filter((_, i) => i !== index));
  };

  return (
    <div className="search-page">
      <div className="search-page-header">
        <h1>MediQuery</h1>
      </div>
      <div className='search-input-dropdown-wrapper'>
        <div className="search-container">
          <input
            type="text"
            className="search-input"
            placeholder="Search..."
            value={searchTerm}
            onChange={handleInputChange}
          />
          <input
            type="number"
            className="year-input"
            placeholder="Start Year"
            value={startYear}
            onChange={handleYearChange(setStartYear)}
          />
          <input
            type="number"
            className="year-input"
            placeholder="End Year"
            value={endYear}
            onChange={handleYearChange(setEndYear)}
          />
          <Link to="/search-results" state={ JSON.stringify({searchTerm, startYear, endYear, saveHistory: true}) }>
            <div className="search-icon-container">
              <img src={search} alt="Search Icon" className="search-icon" />
            </div>
          </Link>
        </div>
        {isDropDownOpen && (
          <div className="search-dropdown" style={{display : `${searchResults.length > 0 ? 'unset' : 'none'}`}}>
            {searchHistory.map((term, index) => (
              <Link to="/search-results" key={index} state={ JSON.stringify({searchTerm: term, saveHistory: false})}>
                <div className="search-dropdown-item-history" onClick={() => setSearchTerm(term)}>
                  <div className='search-dropdown-item-history-text'>{term}</div>
                  <div className="delete-history" onClick={(e) => { e.stopPropagation(); handleDeleteHistory(index); }}>X</div>
                </div>
              </Link>
            ))}
            {searchResults.map((result, index) => (
              <Link to="/search-results" key={index}  state={ JSON.stringify({searchTerm: result, saveHistory: false})}>
                <div className="search-dropdown-item">{result}</div>
              </Link>
            ))}
          </div>
        )}
      </div>
      <footer className="footer">
        <div className="footer-content">
          <p>
            The MediQuery Information Retrieval System is designed to revolutionize online health-related information retrieval...
          </p>
        </div>
      </footer>
    </div>
  );
};

export default SearchPage;

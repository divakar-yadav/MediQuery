import React, { useState, useEffect } from 'react';
import './SearchResultPage.css'; // Import your CSS file
import axios from 'axios';
import { useLocation } from 'react-router-dom';
import { Link } from 'react-router-dom';

const SearchResultPage = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [drpDownsearchResults, setdropDownSearchResults] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [isDropDownOpen, setIsDropDownOpen] = useState(false);
  const resultsPerPage = 10; // Number of results to display per page
  const { state } = useLocation();
  const [searchHistory, setSearchHistory] = useState([]);

  const chips = ['term1','term2','term3','term4']

  useEffect(() => {
    setIsDropDownOpen(false)
    setSearchTerm(JSON.parse(state).searchTerm);
    if (JSON.parse(state).searchTerm.trim() !== "") {
      let data = JSON.stringify({
        "searchTerm": JSON.parse(state).searchTerm,
        "lookupType": "Researcher"
      });

      let config = {
        method: 'post',
        maxBodyLength: Infinity,
        url: 'http://3.144.94.68:8080/search',
        headers: {
          'Content-Type': 'application/json'
        },
        data: data
      };

      axios.request(config)
        .then((response) => {
          setSearchResults(response.data.result);
          console.log(JSON.stringify(response.data));
        })
        .catch((error) => {
          console.log(error);
        });
    }
  }, [state]);

  const handleDeleteHistory = (index) => {
    setSearchHistory(prevHistory => prevHistory.filter((_, i) => i !== index));
  };

  const fetchSearchResults = async () => {
    try {
      const response = await axios.post('http://3.144.94.68:8080/search', {
        searchTerm: searchTerm,
        lookupType: 'lookupType' // Use the selected lookup type
      });
      if (response.status === 200) {
        setSearchResults(response.data.result); // Update search results
        // Add current search term to search history
        setSearchHistory(prevHistory => [searchTerm, ...prevHistory.filter(item => item !== searchTerm)]);
      } else {
        console.error('Failed to fetch search results');
      }
    } catch (error) {
      console.error('Error fetching search results:', error);
    }
  };

  const fetchDropDownSearchResults = async () => {
    try {
      const response = await axios.post('http://3.144.94.68:8080/search', {
        searchTerm: searchTerm,
        lookupType: 'lookupType' // Use the selected lookup type
      });
      if (response.status === 200) {
        setdropDownSearchResults(response.data.result); // Update search results
        // Add current search term to search history
        // setSearchHistory(prevHistory => [searchTerm, ...prevHistory.filter(item => item !== searchTerm)]);
        setIsDropDownOpen(true)
      } else {
        console.error('Failed to fetch search results');
      }
    } catch (error) {
      console.error('Error fetching search results:', error);
    }
  };
  const handleInputChange = async (event) => {
    setSearchTerm(event.target.value);
    // Call API to fetch search results
    fetchDropDownSearchResults(event.target.value);
  };


  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  const indexOfLastResult = currentPage * resultsPerPage;
  const indexOfFirstResult = indexOfLastResult - resultsPerPage;
  const currentResults = searchResults.slice(indexOfFirstResult, indexOfLastResult);

  return (
    <div className="search-results">
      <div className='search-box-sidenav'>
        <div className="search-page-header">
          <h3>MediQuery</h3>
        </div>
      </div>
      <div className='search-box-contain'>
        <h3 className='search-results-title'>Search Results</h3>
        <div className="search-box">
          <input
            type="text"
            value={searchTerm}
            onChange={handleInputChange}
            placeholder="Search..."
            className="search-input"
          />
          <button className="search-button-result" onClick={() => {fetchSearchResults()}}>
            Search
          </button>
          {isDropDownOpen ? 
                    <div className="search-dropdown-result-page" style={{display : `${drpDownsearchResults.length > 0 ? 'unset' : 'none'}`}}>
                    {searchHistory.map((term, index) => (
                      <div key={index} className="search-dropdown-item-history" onClick={() => setIsDropDownOpen(false)}>
                        <Link to="/search-results" key={index}><div className='search-dropdown-item-history-text'>{term}</div></Link>
                        <div className="delete-history" onClick={(e) => { e.stopPropagation(); handleDeleteHistory(index); }}>X</div>
                      </div>
                    ))}
                    {drpDownsearchResults.map((result, index) => (
                      <div className='search-result-drp-down' onClick={()=>setSearchHistory([])}>
                      <Link to="/search-results" key={index}  state={ JSON.stringify({searchTerm: result.briefTitle}) }>
                        <div className="search-dropdown-item">{result.briefTitle}</div>
                      </Link>
                      </div>
                    ))}
                </div>
          : null}

        </div>
        <ul className="results-list">
          {currentResults.length > 0 ? currentResults.map((result, index) => (
            <li key={index} className="result-item">
              <h2 href={'https://scholar.google.com'}>{result.briefTitle}</h2>
              <p>{result.briefSummary}</p>
              {/* Adding chips */}
              <div className="chips-container">
                {chips.map((chip, chipIndex) => (
                  <span key={chipIndex} className="chip">{chip}</span>
                ))}
              </div>
            </li>
          )) : 'No Results found'}
        </ul>
        <div className="pagination">
          {Array.from({ length: Math.ceil(drpDownsearchResults.length / resultsPerPage) }, (_, i) => (
            <button key={i} onClick={() => paginate(i + 1)}>{i + 1}</button>
          ))}
        </div>
      </div>
    </div>
  );
}

export default SearchResultPage;

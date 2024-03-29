import React, { useState } from 'react';
import './SearchPage.css';
import search from '../../assets/images/search.svg';
import axios from 'axios';
//import { useHistory } from 'react-router-dom';

const SearchPage = () => {

//  const history = useHistory();
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [lookupType, setLookupType] = useState('Researcher'); // Default lookup type

  const handleInputChange = async (event) => {
    setSearchTerm(event.target.value);
    // Call API to fetch search results
    fetchSearchResults(event.target.value);
  };

  const handleRadioChange = (event) => {
    setLookupType(event.target.value);
  };
  const handleDropdownClick = () => {
    // Redirect to search results page
//    history.push('/search-results');
  };
  const fetchSearchResults = async (term) => {
    try {
      const response = await axios.post('http://3.144.94.68:8080/search', {
        searchTerm: term,
        lookupType: lookupType // Use the selected lookup type
      });
      if (response.status === 200) {
        setSearchResults(response.data.result); // Update search results
      } else {
        console.error('Failed to fetch search results');
      }
    } catch (error) {
      console.error('Error fetching search results:', error);
    }
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
                <div className="search-icon-container">
                <img src={search} alt="Search Icon" className="search-icon" />
                </div>
            </div>
            <div className="search-dropdown" style={{display : `${searchResults.length > 0 ? 'unset' : 'none'}`}}>
                {searchResults.map((result, index) => (
                <div key={index} className="search-dropdown-item" onClick = {handleDropdownClick}>
                    {result.briefTitle} {/* Display search results here */}
                </div>
                ))}
            </div>
      </div>
      <div className="radio-buttons">
        <input type="radio" id="option1" name="options" value="Information" checked={lookupType === 'Information'} onChange={handleRadioChange} />
        <label htmlFor="option1">Information Purpose</label>

        <input type="radio" id="option2" name="options" value="Researcher" checked={lookupType === 'Researcher'} onChange={handleRadioChange} />
        <label htmlFor="option2">Research Purpose</label>
      </div>
      <footer className="footer">
        <div className="footer-content">
          <p>
            The MediQuery Information Retrieval System is an innovative platform designed to revolutionize online health-related information retrieval. By leveraging cutting-edge search algorithms and advanced natural language processing (NLP) capabilities, this system aims to cater to a diverse audience ranging from the general public seeking health insights to academic professionals and researchers in search of the latest scientific studies and medical breakthroughs. Through a strategic integration of Java and Apache Lucene, MediQuery promises swift, accurate, and contextually relevant search outcomes, marking a significant advancement in the field of digital health information services.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default SearchPage;

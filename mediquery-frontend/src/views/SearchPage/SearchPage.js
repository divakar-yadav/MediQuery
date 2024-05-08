import React, { useState, useEffect } from 'react';
import './SearchPage.css';
import search from '../../assets/images/search.svg';
import axios from 'axios';
import { Link } from 'react-router-dom';
import BrowserV6Field from '../../components/BrowserV6Field/BrowserV6Field'
import dayjs from 'dayjs';


const SearchPage = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [lookupType, setLookupType] = useState('Researcher'); // Default lookup type
  const [searchHistory, setSearchHistory] = useState([]);
  const [isDropDownOpen, setIsDropDownOpen] = useState(false);
  const [startYear, setStartYear] = useState('2000');
  const [endYear, setEndYear] = useState('2022');
  const [selectedJournals, setSelectedJournals] = useState({});
  const [showAllJournals, setShowAllJournals] = useState(true);



  const journals = [
    "JMIR Public Health and Surveillance",
    "Frontiers in Immunology",
    "Cell Reports Medicine",
    "Molecular Cancer",
    "Bioscience Reports",
    "International Journal of Molecular Sciences",
    "Journal of Translational Medicine",
    "Scientific Reports",
    "PLoS ONE",
    "British Journal of Cancer",
    "BMC Cancer",
    "BMC Public Health",
    "Biomolecules",
    "Pharmacological Research",
    "Journal of Cellular and Molecular Medicine",
    "Journal of Experimental & Clinical Cancer Research",
    "Nature Communications",
    "Frontiers in Oncology"
  ];

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


  const fetchSearchResults = async (term) => {
    try {
      const response = await axios.get(`http://3.144.94.68:8080/suggest?query=${term}`, {
        params: {
          searchTerm: term,
          lookupType: lookupType,
          startYear: startYear,
          endYear: endYear,        }
      });
      if (response.status === 200) {
        console.log(response,"-----response-----");
        setSearchResults(response.data);
          
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

  const handleYearChange = (setter) => (event) => {
    setter(event.target.value);
  };

  const handleJournalChange = (journal) => {
    setSelectedJournals(prev => ({
      ...prev,
      [journal]: !prev[journal]
    }));
  };

  const toggleShowAllJournals = () => {
    setShowAllJournals(!showAllJournals);
  };

  const textFieldStyles = {
    width: '200px', // Adjust width as needed
    height: '60px' // Adjust height as needed
};
const handleStartYearChange = (newValue) => {
  setStartYear(newValue.format('YYYY'));  // Assuming newValue is a dayjs object
};

const handleEndYearChange = (newValue) => {
  setEndYear(newValue.format('YYYY'));  // Assuming newValue is a dayjs object
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
          <Link to="/search-results" state={ JSON.stringify({searchTerm : searchTerm, startYear :startYear, endYear :endYear,selectedJournals : selectedJournals, saveHistory: true}) }>
            <div className="search-icon-container">
              <img src={search} alt="Search Icon" className="search-icon" />
            </div>
          </Link>
        </div>
        {isDropDownOpen && (
          <div className="search-dropdown" style={{display : `${searchResults.length > 0 ? 'unset' : 'none'}`}}>
            {searchHistory.map((term, index) => (
              <Link to="/search-results" key={index} state={ JSON.stringify({searchTerm : searchTerm, startYear : startYear, endYear : endYear,selectedJournals : selectedJournals, saveHistory: false})}>
                <div className="search-dropdown-item-history" onClick={() => setSearchTerm(term)}>
                  <div className='search-dropdown-item-history-text'>{term}</div>
                  <div className="delete-history" onClick={(e) => { e.stopPropagation(); handleDeleteHistory(index); }}>X</div>
                </div>
              </Link>
            ))}
            {searchResults.map((result, index) => (
              <Link to="/search-results" key={index}  state={ JSON.stringify({searchTerm : searchTerm, startYear : startYear, endYear : endYear,selectedJournals : selectedJournals, saveHistory: false})}>
                <div className="search-dropdown-item">{result}</div>
              </Link>
            ))}
          </div>
        )}
          <div className="filter-container">
      <span className='filter-text'>Filter by Journals</span>
        <div className='filters-wrapper'>
            {journals.slice(0, showAllJournals ? journals.length : 4).map(journal => (
                <label key={journal}>
                  <input
                      type="checkbox"
                      checked={selectedJournals[journal] || false}
                      onChange={() => handleJournalChange(journal)}
                  /> {journal}
                </label>
            ))}
        </div>
        {/* <div className='show-more-button'>
        {journals.length > 4 && (
                <button variant="text" onClick={toggleShowAllJournals}>
                  {showAllJournals ? 'Show Less' : 'Show More'}
                </button>
            )}
        </div> */}
        <div className='year-filter'>
          <div className='dt-picker'>
            <span>Start date</span>
          <BrowserV6Field
          value = {dayjs(startYear)}
          onChange = {handleStartYearChange}
          />
          </div>
           
          <div className='dt-picker'>
            <span>End date</span>
          <BrowserV6Field
          value = {dayjs(endYear)}
          onChange = {handleEndYearChange}
          />
          </div>            
          </div>
          </div>
      </div>
      <footer className="footer">
        <div className="footer-content">
          <p>
          The MediQuery Information Retrieval System is an innovative platform designed to revolutionize online health-related information retrieval. By leveraging cutting-edge search algorithms and advanced natural language processing (NLP) capabilities, this system aims to cater to a diverse audience ranging from the general public seeking health insights to academic professionals and researchers in search of the latest scientific studies and medical breakthroughs. Through a strategic integration of Java and Apache Lucene, MediQuery promises swift, accurate, and contextually relevant search outcomes, marking a significant advancement in the field of digital health information services.          </p>
        </div>
      </footer>
    </div>
  );
};

export default SearchPage;

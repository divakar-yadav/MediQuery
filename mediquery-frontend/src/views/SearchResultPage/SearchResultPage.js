import React, { useState, useEffect } from 'react';
import './SearchResultPage.css'; // Import your CSS file
import axios from 'axios';
import { useLocation } from 'react-router-dom';
import { Link } from 'react-router-dom';
import citation from '../../assets/images/cite.png'
import published from '../../assets/images/published.png'
import doi from '../../assets/images/doi.jpeg'
import BrowserV6Field from '../../components/BrowserV6Field/BrowserV6Field'
import dayjs from 'dayjs';
import CircularIndeterminate from '../../components/BrowserV6Field/Spinner/Spinner'

const SearchResultPage = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [drpDownsearchResults, setdropDownSearchResults] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [isDropDownOpen, setIsDropDownOpen] = useState(false);
  const resultsPerPage = 10; // Number of results to display per page
  const { state } = useLocation();
  const [searchHistory, setSearchHistory] = useState([]);
  const [startYear, setStartYear] = useState('2000');
  const [endYear, setEndYear] = useState('2024');
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

  useEffect(() => {
    setIsDropDownOpen(false)
    setSearchTerm(JSON.parse(state).searchTerm);
    setStartYear(JSON.parse(state).startYear)
    setEndYear(JSON.parse(state).endYear)
    setSelectedJournals(JSON.parse(state).selectedJournals)
    if(JSON.parse(state).saveHistory){
      setSearchHistory(prevHistory => [JSON.parse(state).searchTerm, ...prevHistory.filter(item => item !== searchTerm)]);
    }
    if (JSON.parse(state)?.searchTerm?.trim() !== "") {
      let data = JSON.stringify({
        "searchTerm": JSON.parse(state).searchTerm,
        "lookupType": "Researcher",
        startYear: JSON.parse(state).startYear,
        endYear: JSON.parse(state).endYear,
        journals : JSON.parse(state).selectedJournals
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
  }, []);

  const handleDeleteHistory = (index) => {
    setSearchHistory(prevHistory => prevHistory.filter((_, i) => i !== index));
  };

  const getInitialWords = (inputString, wordCount) => {
    let words = inputString.split(' ');
    if (words.length > wordCount) {
      return words.slice(0, wordCount).join(' ') + '...';
    } else {
      return inputString;
    }
  }

  const fetchSearchResults = async () => {
    try {
      const response = await axios.post('http://3.144.94.68:8080/search', {
        searchTerm: searchTerm,
        lookupType: 'lookupType',
        startYear: startYear,
        endYear: endYear,
        journals : selectedJournals
        // Use the selected lookup type
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

  const handleSearchResults = async (searchTerm) => {
    setSearchResults([])
    setIsDropDownOpen(false)
    setSearchHistory([])
    setSearchTerm(searchTerm)
    try {
      const response = await axios.post('http://3.144.94.68:8080/search', {
        searchTerm: searchTerm,
        lookupType: 'lookupType',
        startYear: startYear,
        endYear: endYear,
        journals : selectedJournals
        // Use the selected lookup type
      });
      if (response.status === 200) {
        setSearchResults(response.data.result); // Update search results
        // Add current search term to search history
        // setSearchHistory(prevHistory => [searchTerm, ...prevHistory.filter(item => item !== searchTerm)]);
      } else {
        console.error('Failed to fetch search results');
      }
    } catch (error) {
      console.error('Error fetching search results:', error);
    }
  };

  const fetchDropDownSearchResults = async () => {
    try {
      const response = await axios.get(`http://3.144.94.68:8080/suggest?query=${searchTerm}`, {
        searchTerm: searchTerm,
        lookupType: 'lookupType' // Use the selected lookup type
      });
      if (response.status === 200) {
            setdropDownSearchResults(response.data);
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

  const handleStartYearChange = (newValue) => {
    setStartYear(newValue.format('YYYY'));  // Assuming newValue is a dayjs object
  };
  
  const handleEndYearChange = (newValue) => {
    setEndYear(newValue.format('YYYY'));  // Assuming newValue is a dayjs object
  };

  const handleJournalChange = (journal) => {
    setSelectedJournals(prev => ({
      ...prev,
      [journal]: !prev[journal]
    }));
  };
  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  const indexOfLastResult = currentPage * resultsPerPage;
  const indexOfFirstResult = indexOfLastResult - resultsPerPage;
  const currentResults = searchResults.slice(indexOfFirstResult, indexOfLastResult);

  const textFieldStyles = {
    width: '20px', // Adjust width as needed
    height: '20px', // Adjust height as needed
    padding: '10px 12px'  // Adjust padding to vertically center the text, if necessary
};
  return (
    <div className="search-results">
      <div className='search-box-sidenav'>
        <div className="search-page-header">
          <h3>MediQuery</h3>
        </div>
        <div className='year-filter-sr'>
          <div className='dt-picker-sr'>
            <span>Start date</span>
          <BrowserV6Field
          value = {dayjs(startYear)}
          onChange = {handleStartYearChange}
          />
          </div>
           
          <div className='dt-picker-sr'>
            <span>End date</span>
          <BrowserV6Field
          value = {dayjs(endYear)}
          onChange = {handleEndYearChange}
          />
          </div>            
          
          </div>
        <div className='filters-wrapper-sr'>
          <span className='filters-wrapper-title-sr'>Filter by Journals</span>
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
                      // <Link to="/search-results" key={index}>
                      <div key={index} className="search-dropdown-item-history" onClick={() => {handleSearchResults(term)}}>
                        <div className='search-dropdown-item-history-text'>{term}</div>
                        <div className="delete-history" onClick={(e) => { e.stopPropagation(); handleDeleteHistory(index); }}>X</div>
                      </div>
                      // </Link>
                    ))}
                    {drpDownsearchResults.map((result, index) => (
                      // <Link to="/search-results" key={index}  state={ JSON.stringify({searchTerm: result, saveHistory : false})}>
                      <div className='search-result-drp-down' onClick={() => {handleSearchResults(result)}}>
                        <div className="search-dropdown-item">{result}</div>
                      </div>
                      // </Link>
                    ))}
                </div>
          : null}
        </div>
        <ul className="results-list">
          {currentResults.length > 0 ? currentResults.map((result, index) => (
            <li key={index} className="result-item">
              <a href={result?.url}>{result.title}</a>
              <div className='meta'>
                <div className="author-container">
                {result.authors.slice(0,3).map((chip, chipIndex) => (
                  <span key={chipIndex} className="author">{chip}</span>
                ))}
              </div>
              </div>
              {/* <p> {getInitialWords(result.abstract, 100)}</p> */}
              <p dangerouslySetInnerHTML={{ __html: result.snippet }} />

              {/* Adding chips */}
              <div className="chips-container">
                <img className='published-icon' src = {doi} />
                  <a href={result.doi} className="doi">doi</a>
                  <div className='published-wrapper'>
                    <img className='published-icon' src = {published} />
                    <span  className="published-year">Published year</span>
                    <span  className="published-year">{result.published_year}</span>
                  </div>
                  <div className='citation-wrapper'>
                    <img className='citation-by-icon' src = {citation} />
                    <span  className="published-year">Cited by</span>
                    <span  className="citation-by">{result.citation_count}</span>
                  </div>

              </div>
            </li>
          )) : <CircularIndeterminate/>}
        </ul>
        {/* <div className="pagination">
          {Array.from({ length: Math.ceil(currentResults.length / resultsPerPage) }, (_, i) => (
            <button key={i} onClick={() => paginate(i + 1)}>{i + 1}</button>
          ))}
        </div> */}
        <div className="pagination">
  {Array.from({ length: Math.ceil(searchResults.length / resultsPerPage) }, (_, i) => (
    <button key={i} onClick={() => paginate(i + 1)}
    >{i + 1}</button>
  ))}
</div>

      </div>
    </div>
  );
}

export default SearchResultPage;

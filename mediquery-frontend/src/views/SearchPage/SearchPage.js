import './SearchPage.css';
import search from '../../assets/images/search.svg'
const SearchPage = ()=> {
  return (
<div class="search-page">
    <div class="search-page-header">
        <h1>MediQuery</h1>
    </div>
    <div class="search-container">
        <input type="text" class="search-input" placeholder="Search..."/>
        <div class="search-icon-container">
            <img src={search} alt="Search Icon" class="search-icon"/>
        </div>
    </div>
    <div class="radio-buttons">
        <input type="radio" id="option1" name="options" checked/>
        <label for="option1">Information Purpose</label>

        <input type="radio" id="option2" name="options"/>
        <label for="option2">Research Purpose</label>
    </div>

</div>

  );
}

export default SearchPage;
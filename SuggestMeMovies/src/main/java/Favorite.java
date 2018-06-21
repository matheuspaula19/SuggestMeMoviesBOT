
public class Favorite {
	private String movieId;
	private String movieTitle;
	private String movieRating;
	private String movieImdbUrl;
	private String movieTrailer;	
	private String posterUrl;
	private String movieSummary;
	private int movieYear;
	
	public Favorite(String id, String title, String rating, int year, String imdburl, String posterurl, String movietrailer, String summary) {
		this.movieId = id;
		this.movieTitle = title;
		this.movieRating = rating;
		this.movieYear = year;
		this.movieImdbUrl = imdburl;
		this.posterUrl = posterurl;
		this.movieTrailer = movietrailer;
		this.movieSummary = summary;
	}
	
	//getters and setters
	public String getMovieSummary() {
		return movieSummary;
	}
	public void setMovieSummary(String movieSummary) {
		this.movieSummary = movieSummary;
	}
	public String getMovieTrailer() {
		return movieTrailer;
	}
	public void setMovieTrailer(String movieTrailer) {
		this.movieTrailer = movieTrailer;
	}
	public String getMovieId() {
		return movieId;
	}
	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}
	public String getMovieRating() {
		return movieRating;
	}
	public void setMovieRating(String movieRating) {
		this.movieRating = movieRating;
	}
	public String getMovieImdbUrl() {
		return movieImdbUrl;
	}
	public void setMovieImdbUrl(String movieImdbUrl) {
		this.movieImdbUrl = movieImdbUrl;
	}
	public String getPosterUrl() {
		return posterUrl;
	}
	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}
	public int getMovieYear() {
		return movieYear;
	}
	public void setMovieYear(int movieYear) {
		this.movieYear = movieYear;
	}
	public String getMovieTitle() {
		return movieTitle;
	}
	public void setMovieTitle(String movieTitle) {
		this.movieTitle = movieTitle;
	}
	public String getImdbUrl() {
		return movieImdbUrl;
	}
	public void setImdbUrl(String imdbUrl) {
		this.movieImdbUrl = imdbUrl;
	}
}

package fi.tuukka.weather.model;

public interface ModelInterface {

	/**
	 * @return false if something downloadable is still missing
	 */
	public boolean isFinished();
	
	/**
	 * Download the next downloadable thing.
	 */
	public void downloadNext();
	
}

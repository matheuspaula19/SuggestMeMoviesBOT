import java.util.List;
import java.util.Map;

public interface Subject {
	
	public void registerObserver(Observer observer);
	
	public void notifyObservers(long chatId, String responseData, String responseImage, List<Map<String, String>> queryResult);

}

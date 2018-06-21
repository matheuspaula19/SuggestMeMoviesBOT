import java.util.List;
import java.util.Map;

public interface Observer {

	public void update(long chatId, String resposeData, String responseImage, String responseItem, List<Map<String, String>> queryResult);
	
}

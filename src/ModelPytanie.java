import java.io.Serializable;

public class ModelPytanie implements Serializable {
	public String pytanie;
	public String odpowiedz;
	
	public ModelPytanie(String pytanie, String odpowiedz){
		this.pytanie = pytanie;
		this.odpowiedz = odpowiedz;
		/*pytanie = "Pytanie: Ile kropek ma biedronka?";
		odpowiedz = "7";*/
	}
	
	public String getPytanie() {
        return pytanie;
    }
	
	public String getOdpowiedz() {
		return odpowiedz;
	}

}

package Proj1;

public class ModelPytanie {
	public String pytanie;
	public String odpowiedz;
	
	public ModelPytanie(){
		pytanie = "Pytanie: Ile kropek ma biedronka?";
		odpowiedz = "7";
	}
	
	public String getPytanie() {
        return pytanie;
    }
	
	public String getOdpowiedz() {
		return odpowiedz;
	}

}




//Program pozwala na wyeksportowanie pytań z pliku do bazy danych Firebase
import java.io.*;
import java.util.Scanner;

import com.firebase.client.Firebase;

public class AddQuestionsToDB {
	private static Firebase jFirebase =  new Firebase("https://quiz-61ac7.firebaseio.com/questions");
	
	
	static void odczyt (int x, File a, File b) throws FileNotFoundException, IOException {
		
		Scanner in1 = new Scanner(a);
		Scanner in2 = new Scanner(b);
		String pytanie, odpowiedz;		
		
		System.out.println("Zapisywanie puli pytań do bazy danych:");
		for (int i = 0; i < x; i++) {
			pytanie = in1.nextLine();
			odpowiedz = in2.nextLine();
			System.out.println(pytanie + " -> " + odpowiedz);
			zapisz(i+1, pytanie, odpowiedz);
		}
		
		//zakończenie pracy skanera
		in1.close();
		in2.close();
		
	}
	
	static void zapisz(int numerPytania, String pytanie, String odpowiedz) {
		
		Firebase qs = jFirebase.child(Integer.toString(numerPytania));
		
		ModelPytanie q1 = new ModelPytanie(pytanie, odpowiedz);
		System.out.println(numerPytania + ". " + q1.pytanie + q1.odpowiedz);
		qs.setValue(q1);
			
		
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		System.out.print("Ile pytań jest do wgrania do bazy danych?");
		Scanner podanie = new Scanner(System.in);
		int n = podanie.nextInt();
		podanie.close();
		
		File file1 = new File("pytania.txt");
		File file2 = new File("odpowiedzi.txt");
		
		odczyt(n, file1, file2);
		
	}
	
}